package apros.codeart.ddd.saga.internal;

import apros.codeart.ddd.saga.EventLogFactory;
import apros.codeart.ddd.saga.RaisedEntry;
import apros.codeart.dto.DTObject;

public final class EventLog {
	private EventLog() {
	}

	/**
	 * 
	 * 开始触发事件队列
	 * 
	 * @param queueId
	 */
	public static void writeRaiseStart(String queueId) {
		var log = EventLogFactory.getLog();
		log.writeRaiseStart(queueId);
	}

	/**
	 * 
	 * 写入要执行事件 {@eventName} 的日志
	 * 
	 * @param queue
	 * @param entry
	 */
	public static void writeRaise(String queueId, String eventName) {
		var log = EventLogFactory.getLog();
		log.writeRaise(queueId, eventName);
	}

	public static void writeRaiseLog(String queueId, String eventName, DTObject log) {
		var logger = EventLogFactory.getLog();
		logger.writeRaiseLog(queueId, eventName, log);
	}

	/**
	 * 
	 * 写入事件已经全部触发完毕的日志
	 * 
	 * @param queueId
	 */
	public static void writeRaiseEnd(String queueId) {
		var log = EventLogFactory.getLog();
		log.writeRaiseEnd(queueId);
	}

	public static void writeReverseStart(String queueId) {
		var log = EventLogFactory.getLog();
		log.writeReverseStart(queueId);
	}

	/**
	 * 
	 * 得到已经执行了的事件队列（注意，最后执行的事件在队列的第一项）
	 * 
	 * @param queueId
	 * @return
	 */
	public static RaisedQueue findRaised(String queueId) {
		var log = EventLogFactory.getLog();
		var entries = log.findRaised(queueId);
		return new RaisedQueue(queueId, entries);
	}

	/**
	 * 
	 * 记录事件已被回溯（对于本地事件，是成功回溯，对于远程事件，是已经成功发送回溯通知）
	 * 
	 * @param queueId
	 * @param eventId
	 */
	public static void writeReversed(RaisedEntry entry) {
		var log = EventLogFactory.getLog();
		log.writeReversed(entry);
	}

	/**
	 * 
	 * 记录事件已经全部回溯
	 * 
	 * @param queueId
	 */
	public static void writeReverseEnd(String queueId) {
		var log = EventLogFactory.getLog();
		log.writeReverseEnd(queueId);
	}

}
