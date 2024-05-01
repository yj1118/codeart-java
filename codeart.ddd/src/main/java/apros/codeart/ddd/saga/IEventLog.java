package apros.codeart.ddd.saga;

import java.util.List;

import apros.codeart.dto.DTObject;

public interface IEventLog {

	/**
	 * 
	 * 获得一个新的日志唯一标识（也就是事件队列编号）
	 * 
	 * @return
	 */
	public String newId();

	/**
	 * 
	 * 开始触发事件队列
	 * 
	 * @param queueId
	 */
	void writeRaiseStart(String queueId);

	/**
	 * 
	 * 写入要执行事件 {@eventName} 的日志
	 * 
	 * @param queue
	 * @param entry
	 */
	void writeRaise(String queueId, String eventName);

	/**
	 * 
	 * 执行时需要记录的日志信息，当恢复的时候，需要读取该日志
	 * 
	 * @param queueId
	 * @param eventName
	 * @param log
	 */
	void writeRaiseLog(String queueId, String eventName, DTObject log);

	/**
	 * 
	 * 写入事件已经全部触发完毕的日志
	 * 
	 * @param queueId
	 */
	void writeRaiseEnd(String queueId);

	/**
	 * 
	 * 写入开始回溯的事件日志
	 * 
	 * @param queueId
	 */
	void writeReverseStart(String queueId);

	/**
	 * 
	 * 得到已经执行了的事件队列（注意，按照执行顺序倒序返回，比如：最后执行的事件在队列的第一项）
	 * 
	 * @param queueId
	 * @return
	 */
	List<RaisedEntry> findRaised(String queueId);

	/**
	 * 
	 * 记录事件已被回溯（对于本地事件，是成功回溯，对于远程事件，是已经成功发送回溯通知）
	 * 
	 * @param queueId
	 * @param eventId
	 */
	void writeReversed(RaisedEntry entry);

	/**
	 * 
	 * 记录事件已经全部回溯
	 * 
	 * @param queueId
	 */
	void writeReverseEnd(String queueId);

	/**
	 * 
	 * 找到由于中断的原因要恢复的事件队列编号
	 * 
	 * @param top
	 * @return
	 */
	List<String> findInterrupteds();

	/**
	 * 清理过期的日志
	 */
	void clean();

}
