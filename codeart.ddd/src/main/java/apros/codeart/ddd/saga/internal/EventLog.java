package apros.codeart.ddd.saga.internal;

import apros.codeart.ddd.saga.internal.protector.EventReverseQueue;
import apros.codeart.ddd.saga.internal.trigger.EventContext;
import apros.codeart.dto.DTObject;

public final class EventLog {
	private EventLog() {
	}

	/**
	 * 
	 * 在触发事件之前，写入一次日志
	 * 
	 * @param queue
	 * @param entry
	 */
	public static void flushRaise(EventContext ctx) {
		var logId = ctx.id();
		// 写入日志
		var content = DTObject.editable();
		content["entryId"] = ctx.eventId();
		EventLog.write(logId, EventOperation.Raise, content);
	}

	/// <summary>
	/// 写入并提交被执行完毕的日志
	/// </summary>
	/// <param name="queue"></param>
	/// <param name="entry"></param>
	public static void flushEnd(EventContext ctx) {
		var logId = queueId;
		// 写入日志
		EventLog.FlushWrite(logId, EventOperation.End, DTObject.Empty);
	}

	public static EventReverseQueue find(String queueId) {

	}

}
