package apros.codeart.rabbitmq.rpc;

import java.util.function.Function;

import apros.codeart.rabbitmq.MQConnConfig;
import apros.codeart.rabbitmq.Policy;
import apros.codeart.rabbitmq.RabbitMQConfig;
import apros.codeart.util.LazyIndexer;

final class RPC {

	private RPC() {
	}

	/**
	 * 事件采用的消息的策略
	 */
	public static final MQConnConfig ConnConfig;

	public static final Policy ServerPolicy;

	public static final Policy ClientPolicy;

	static {
		ConnConfig = RabbitMQConfig.find("rpc");

		// rpc不需要那么高的可靠性，所以不需要发布者确认，也不需要消息持久化
		// 每一个server处理1条消息，处理完后再执行下一条消息
		// 但是可以建立多个server来提高吞吐量，共同承担处理消息的任务
		ServerPolicy = new Policy(ConnConfig, 1, false, false);
		// 客户端是临时队列消费，也一样
		ClientPolicy = new Policy(ConnConfig, 1, false, false);
	}

	public static String getServerQueue(String method) {
		return _getServerQueue.apply(method);
	}

	private static Function<String, String> _getServerQueue = LazyIndexer.init((method) -> {
		return String.format("rpc-%s", method.toLowerCase());
	});
}
