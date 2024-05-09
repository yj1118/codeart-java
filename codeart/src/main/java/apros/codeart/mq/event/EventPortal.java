package apros.codeart.mq.event;

import apros.codeart.InterfaceImplementer;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.FactorySetting;

public final class EventPortal {

	private EventPortal() {
	}

	/**
	 * 
	 * 当事件有订阅时发布事件
	 * 
	 * @param eventName 事件的名称
	 * @param arg       事件参数
	 */
	public static void publish(String eventName, DTObject arg) {
		var publisher = createPublisher();
		publisher.publish(eventName, arg);
	}

	private static IPublisher createPublisher() {
		return getPublisherFactory().create();
	}

	/**
	 * @param eventName
	 * @param handler
	 * @param cluster   是否需要集群支持
	 */
	public static void subscribe(String eventName, IEventHandler handler, boolean cluster) {
		subscribe(eventName, handler, cluster, false);
	}

	/**
	 * 订阅远程事件
	 * 
	 * @param eventName
	 * @param handler
	 * @param startUp   false:不立即启动订阅器，由全局方法StartUp统一调度，适用于程序初始化期间挂载订阅,true:立即启动订阅器
	 */
	public static void subscribe(String eventName, IEventHandler handler, boolean cluster, boolean startUp) {
		var subscriber = createSubscriber(eventName, cluster);
		subscriber.addHandler(handler);
		if (startUp)
			subscriber.accept();
	}

	private static ISubscriber createSubscriber(String eventName, boolean cluster) {
		return getSubscriberFactory().create(eventName, cluster);
	}

	private static ISubscriber removeSubscriber(String eventName) {
		return getSubscriberFactory().remove(eventName);
	}

	public static void cancel(String eventName) {
		var subscriber = getSubscriberFactory().get(eventName);
		subscriber.stop();
	}

	/**
	 * 启动订阅器
	 */
	static void startUp() {
		var subscribers = getSubscriberFactory().getAll();
		for (var subscriber : subscribers) {
			subscriber.accept();
		}
	}

	/**
	 * 移除事件
	 * 
	 * @param eventName
	 */
	public static void remove(String eventName) {
		var subscriber = removeSubscriber(eventName);
		if (subscriber != null)
			subscriber.remove();
	}

//	region 获取和注册工厂

	static IPublisherFactory getPublisherFactory() {
		return _publisherSetting.getFactory();
	}

	private static FactorySetting<IPublisherFactory> _publisherSetting = new FactorySetting<IPublisherFactory>(
			IPublisherFactory.class, () -> {
				InterfaceImplementer impl = MQEvent.getPublisherFactoryImplementer();
				if (impl != null) {
					return impl.getInstance(IPublisherFactory.class);
				}
				return null;
			});

	public static void register(IPublisherFactory factory) {
		_publisherSetting.register(factory);
	}

	static ISubscriberFactory getSubscriberFactory() {
		return _subscriberSetting.getFactory();
	}

	private static FactorySetting<ISubscriberFactory> _subscriberSetting = new FactorySetting<ISubscriberFactory>(
			ISubscriberFactory.class, () -> {
				InterfaceImplementer impl = MQEvent.getSubscriberFactoryImplementer();
				if (impl != null) {
					return impl.getInstance(ISubscriberFactory.class);
				}
				return null;
			});

	public static void register(ISubscriberFactory factory) {
		_subscriberSetting.register(factory);
	}

//	#endregion

}
