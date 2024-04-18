package apros.codeart.mq;

/**
 * 事件的订阅者
 */
public interface ISubscriber {

	/**
	 * 接收事件
	 */
	void accept();

	/**
	 * 停止接收事件
	 */
	void stop();

	/**
	 * 为订阅者添加处理器
	 * 
	 * @param handler
	 */
	void addHandler(IEventHandler handler);

	/**
	 * 释放事件资源
	 */
	void cleanup();
}