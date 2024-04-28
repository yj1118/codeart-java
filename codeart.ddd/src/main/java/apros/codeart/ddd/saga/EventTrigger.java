package apros.codeart.ddd.saga;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import apros.codeart.ddd.repository.DataContext;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPortal;
import apros.codeart.util.concurrent.ISignal;
import apros.codeart.util.concurrent.LatchSignal;

final class EventTrigger {

	private EventTrigger() {
	}

	/**
	 * 
	 * 开始一个新的事件
	 * 
	 * @param source
	 * @param input
	 * @return
	 */
	public static DTObject start(DomainEvent source, DTObject input) {
		var queue = new EventQueue(source, input);
		try {
			return raise(queue);
		} catch (Exception ex) {
			EventProtector.restore(queue.id()); // 恢复
		}
		return DTObject.Empty;
	}

	/**
	 * 
	 * 开始一个新的事件
	 * 
	 * @param queue
	 * @return
	 */
	public static DTObject raise(EventQueue queue) {
		var input = queue.input();
		DTObject args = input.asEditable();
		EventContext ctx = new EventContext(queue.id(), input);

		while (true) {
			// 触发队列事件
			var event = queue.next(args);
			if (event != null) {
				String eventName = event.name();
				ctx.direct(eventName); // 将事件上下文重定向到新的事件上
				EventLog.flushRaise(ctx, eventName); // 一定要确保日志先被正确的写入，否则会有BUG
				args = queue.getArgs(args, ctx);
				if (event.local() != null) {
					// 本地事件，直接执行
					args = raiseLocalEvent(event.local(), args, ctx);
				} else {
					args = raiseRemoteEvent(args, ctx);
				}
			}
			break;
		}

		EventLog.flushEnd(ctx); // 指示恢复管理器事件队列的操作已经全部完成
		return args;
	}

	private static DTObject raiseLocalEvent(DomainEvent event, DTObject args, EventContext ctx) {
		return DataContext.newScope(() -> {
			var output = event.raise(args, ctx);
			// 这里上下文如果保存失败了，那么事务肯定也执行失败，没问题
			// 上下文如果保存成功了，那么当commit提交失败了，由于幂等性，就算执行回溯了，也没事
			ctx.save();
			return output;
		});
	}

	private static DTObject raiseRemoteEvent(DTObject args, EventContext ctx) {

		// 先订阅触发事件的返回结果的事件
		subscribeRemoteEventResult(ctx.eventId());

		// 再发布“触发事件”的事件
		var raiseEventName = EventUtil.getRaise(ctx.eventName());

		var remoteArg = ctx.getEntryRemotable(args);
		EventPortal.publish(raiseEventName, remoteArg); // 触发远程事件就是发布一个“触发事件”的事件
		// 订阅者会收到消息后会执行触发操作

		ISignal<DTObject> signal = createSignal(ctx.eventId());

		try {
			// 等待远程调用的结果
			var output = signal.wait(10, TimeUnit.SECONDS);

			var error = output.getString("error", null);

			if (error != null) {
				throw new RemoteEventFailedException(error);
			}

			return output.getObject("args");

		} catch (Exception ex) {
			throw ex;
		} finally {
			removeSignal(ctx.eventId());
			cleanupRemoteEventResult(ctx.eventId());
		}

	}

	private static void subscribeRemoteEventResult(String eventId) {
		var raiseResultEventName = EventUtil.getRaiseResult(eventId);
		EventPortal.subscribe(raiseResultEventName, ReceiveResultEventHandler.instance, true);
	}

	/**
	 * 
	 * 删除由于接受调用结果而创建的临时队列
	 * 
	 * @param eventId
	 */
	static void cleanupRemoteEventResult(String eventId) {
		var raiseResultEventName = EventUtil.getRaiseResult(eventId);
		EventPortal.cleanup(raiseResultEventName);
	}

	private static ConcurrentHashMap<String, ISignal<DTObject>> _signals = new ConcurrentHashMap<>();

	private static ISignal<DTObject> createSignal(String id) {
		LatchSignal<DTObject> signal = new LatchSignal<>();
		_signals.put(id, signal);
		return signal;
	}

	private static void removeSignal(String id) {
		_signals.remove(id);
	}

	public static ISignal<DTObject> getSignal(String id) {
		return _signals.get(id);
	}

}
