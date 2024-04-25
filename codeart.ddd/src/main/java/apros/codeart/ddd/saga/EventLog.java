package apros.codeart.ddd.saga;

import apros.codeart.dto.DTObject;

final class EventLog {
	private EventLog() {
	}

	/**
	 * 
	 * 在触发事件之前，写入一次日志
	 * 
	 * @param queue
	 * @param entry
	 */
	public static void flushRaise(EventQueue queue, String eventName) {
		var logId = queue.id();
		// 写入日志
		var content = DTObject.Create();
		content["entryId"] = entry.Id;
		EventLog.write(logId, EventOperation.Raise, content);
	}

}
