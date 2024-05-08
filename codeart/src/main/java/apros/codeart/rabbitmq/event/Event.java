package apros.codeart.rabbitmq.event;

import java.util.function.Function;

import apros.codeart.rabbitmq.MQConnConfig;
import apros.codeart.rabbitmq.RabbitMQConfig;
import apros.codeart.util.LazyIndexer;

final class Event {

	private Event() {
	}

	/**
	 * 事件采用的消息的策略
	 */
	public static final MQConnConfig ConnConfig;

	static {
		ConnConfig = RabbitMQConfig.find("event");
	}

	public static String getServerQueue(String method) {
		return _getServerQueue.apply(method);
	}

	private static Function<String, String> _getServerQueue = LazyIndexer.init((method) -> {
		return String.format("rpc-%s", method.toLowerCase());
	});
}