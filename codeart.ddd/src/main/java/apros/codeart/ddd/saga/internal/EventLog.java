package apros.codeart.ddd.saga.internal;

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
	public static void flushRaise(String queueId, String eventId) {
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
	public static void flushRaiseEnd(String queueId) {
		var logId = queueId;
		// 写入日志
		EventLog.FlushWrite(logId, EventOperation.End, DTObject.Empty);
	}

	/**
	 * 
	 * 得到队列已执行的事件的条目信息
	 * 
	 * @param queueId
	 * @return
	 */
	public static RaisedQueue find(String queueId) {

	}

	/**
	 * 
	 * 记录事件已被回溯（对于本地事件，是成功回溯，对于远程事件，是已经成功发送回溯通知）
	 * 
	 * @param queueId
	 * @param eventId
	 */
	public static void flushReverse(String queueId, String eventId) {

	}

	public static void flushReverseEnd(String queueId) {

	}

}
