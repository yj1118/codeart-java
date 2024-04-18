package apros.codeart.mq;

/**
 * 事件订阅器工厂
 */
public interface ISubscriberFactory {

	/**
	 * @param eventName 订阅者订阅的事件
	 * @param group     订阅者所属的分组
	 * @return
	 */
	ISubscriber create(String eventName, String group);

	/**
	 * 
	 * 移除订阅器
	 * 
	 * @param eventName
	 * @param group
	 * @return
	 */
	ISubscriber remove(String eventName, String group);

	Iterable<ISubscriber> getAll();

}
