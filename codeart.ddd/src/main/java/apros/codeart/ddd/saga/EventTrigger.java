package apros.codeart.ddd.saga;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import apros.codeart.UserUIException;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPortal;
import apros.codeart.util.StringUtil;
import apros.codeart.util.concurrent.ISignal;
import apros.codeart.util.concurrent.LatchSignal;

public final class EventTrigger {

	private EventTrigger() {
	}

	/// <summary>
	/// 开始一个新的事件
	/// </summary>
	/// <param name="event"></param>
	public static void start(DomainEvent source, DTObject input, boolean byRemoteInvoke) {
		var queue = new EventQueue(source, input, byRemoteInvoke);
		raise(queue, input);
	}

	private static DTObject raise(EventQueue queue, DTObject input) {
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

		if (queue.byRemoteInvoke()) {
			// 发布事件被完成的消息
			ctx.direct(queue.source().name()); // 将上下文切换到源事件
			publishRaiseSucceeded(args, ctx);
		}

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

			var message = output.getString("message", null);

			if (message != null) {
				throw new UserUIException(message);
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
	 * 发布事件调用成功的结果
	 * 
	 * @param args
	 * @param ctx
	 */
	private static void publishRaiseSucceeded(DTObject args, EventContext ctx) {
		var en = EventUtil.getRaiseResult(ctx.eventId()); // 消息队列的事件名称
		// 返回事件成功被执行的结果
		var arg = createPublishRaiseResultArg(args, ctx, null, true);
		EventPortal.publish(en, arg);
	}

	private static DTObject createPublishRaiseResultArg(DTObject args, EventContext ctx, String error,
			boolean isBusinessException) {
		var output = DTObject.editable();

		output.setString("eventName", ctx.eventName());
		output.setString("eventId", ctx.eventId());

		if (!StringUtil.isNullOrEmpty(error)) {
			if (isBusinessException)
				output.setString("message", error);
			else
				output.setString("error", error);
		}

		output.setObject("args", args);
		output.setObject("identity", ctx.getIdentity());
		return output;
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
