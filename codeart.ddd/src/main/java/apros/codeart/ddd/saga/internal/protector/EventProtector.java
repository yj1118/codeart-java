package apros.codeart.ddd.saga.internal.protector;

import com.google.common.base.Strings;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.ddd.saga.internal.EventLog;
import apros.codeart.ddd.saga.internal.EventUtil;
import apros.codeart.ddd.saga.internal.trigger.EventTrigger;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPortal;

public final class EventProtector {
	private EventProtector() {
	}

	public static void restore(String queueId, boolean isBackground) {

		try {
			// 找到日志
			var queue = EventLog.find(queueId);
			if (queue == null)
				return;

			if (isBackground) {
				AppSession.setIdentity(queue.identity());
			}

			while (true) {
				// 触发回溯序列事件
				var event = queue.next();
				if (event != null) {
					String eventName = event.name();
					if (event.local() != null) {
						// 本地事件，直接执行
						reverseLocalEvent(event.local(), event.log());
					} else {
						reverseRemoteEvent(event.name(), event.id(), queue);
					}
				}
				break;
			}

			EventLog.flushEnd(ctx); // 指示恢复管理器事件队列的操作已经全部完成
			return args;
		} catch (Exception ex) {
			// 恢复期间发生了错误
			var e = new EventRestoreException(string.Format(Strings.RecoveryEventFailed, queueId), ex);
			// 写入日志
			Logger.Fatal(e);
			throw e;
		}
	}

	private static void reverseLocalEvent(DomainEvent event, DTObject log) {
		DataContext.newScope(() -> {
			event.reverse(log);
		});

	}

	private static void reverseRemoteEvent(String eventName, String eventId, EventReverseQueue queue) {
		// 调用远程事件时会创建一个接收结果的临时队列，有可能该临时队列没有被删除，所以需要在回逆的时候处理一次
		EventTrigger.cleanupRemoteEventResult(eventId);

		// 发布“回逆事件”的事件
		var reverseEventName = EventUtil.getReverse(eventName);

		var remotable = DTObject.editable();
		remotable.setString("id", queue.id());
		remotable.setString("eventName", eventName);
		remotable.setObject("identity", queue.identity());

		EventPortal.publish(reverseEventName, remotable);

		// 注意，与“触发事件”不同，我们不需要等待回逆的结果，只用传递回逆的消息即可
	}

}
