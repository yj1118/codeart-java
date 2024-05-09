package apros.codeart.mq.event;

/**
 * 事件订阅器工厂
 */
public interface ISubscriberFactory {

	/**
	 * 
	 * 创建订阅者
	 * 
	 * @param eventName
	 * @param cluster   订阅者是否需要集群支持，对于临时订阅设置为false
	 * @return
	 */
	ISubscriber create(String eventName, boolean cluster);

	ISubscriber get(String eventName);

	/**
	 * 
	 * 移除订阅器
	 * 
	 * @param eventName
	 * @return
	 */
	ISubscriber remove(String eventName);

	Iterable<ISubscriber> getAll();

}
