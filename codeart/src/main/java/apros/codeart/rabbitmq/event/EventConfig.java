package apros.codeart.rabbitmq.event;

import java.util.function.Function;

import apros.codeart.mq.event.MQEvent;
import apros.codeart.rabbitmq.ConsumerConfig;
import apros.codeart.rabbitmq.MQConnConfig;
import apros.codeart.rabbitmq.Policy;
import apros.codeart.rabbitmq.RabbitMQConfig;
import apros.codeart.util.LazyIndexer;

final class EventConfig {

	private EventConfig() {
	}

	/**
	 * 事件采用的消息的策略
	 */
	public static final MQConnConfig ConnConfig;

	public static final String Exchange = "event-exchange";

	public static final Policy PublisherPolicy;

	public static final Policy SubscriberPolicy;

	static {
		ConnConfig = RabbitMQConfig.find("event");

		// 由于事件需要高可靠性，所以我们需要发布者确认模式和持久化消息
		// 对于发布者，prefetchCount的设置没有意义，所以随便设置为1
		PublisherPolicy = new Policy(ConnConfig, 1, true, true);

		SubscriberPolicy = new Policy(ConnConfig, 1, true, true);
	}

	public static String getQueue(String eventName, String group) {
		return String.format("%s-%s", eventName, group);
	}

	/**
	 * 
	 * 获得rpc方法对应得配置信息
	 * 
	 * @param method
	 * @return
	 */
	public static ConsumerConfig getServerConfig(String eventName) {
		return _getServerConfig.apply(eventName);
	}

	private static Function<String, ConsumerConfig> _getServerConfig = LazyIndexer.init((eventName) -> {
		var maxConcurrency = MQEvent.section().getInt(String.format("subscriber.maxConcurrency.%s", eventName), -1);
		if (maxConcurrency < 0)
			maxConcurrency = MQEvent.section().getInt("subscriber.maxConcurrency", 10);

		return new ConsumerConfig(maxConcurrency);
	});
}