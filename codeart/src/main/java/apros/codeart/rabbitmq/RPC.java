package apros.codeart.rabbitmq;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;

public final class RPC {

	private RPC() {
	}

	/**
	 * 事件采用的消息的策略
	 */
	public static final Policy Policy;

	static {
		Policy = RabbitMQConfig.find("rpc", 1, false, false);
	}

	public static String getServerQueue(String method) {
		return _getServerQueue.apply(method);
	}

	private static Function<String, String> _getServerQueue = LazyIndexer.init((method) -> {
		return String.format("rpc-%s", method.toLowerCase());
	});
}
