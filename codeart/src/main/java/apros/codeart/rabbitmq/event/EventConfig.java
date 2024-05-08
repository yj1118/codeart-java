package apros.codeart.rabbitmq.event;

import apros.codeart.rabbitmq.MQConnConfig;
import apros.codeart.rabbitmq.Policy;
import apros.codeart.rabbitmq.RabbitMQConfig;

final class EventConfig {

	private EventConfig() {
	}

	/**
	 * 事件采用的消息的策略
	 */
	public static final MQConnConfig ConnConfig;

	public static final String Exchange = "event-exchange";

	public static final Policy PublisherPolicy;

	static {
		ConnConfig = RabbitMQConfig.find("event");

		// 由于事件需要高可靠性，所以我们需要发布者确认模式和持久化消息
		// 对于发布者，prefetchCount的设置没有意义，所以随便设置为1
		PublisherPolicy = new Policy(ConnConfig, 1, true, true);
	}
}