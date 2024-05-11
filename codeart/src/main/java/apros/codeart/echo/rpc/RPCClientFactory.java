package apros.codeart.echo.rpc;

import apros.codeart.rabbitmq.rpc.RabbitMQRPCClientFactory;

public final class RPCClientFactory {
	private RPCClientFactory() {
	}

	private static IClientFactory _factory = RabbitMQRPCClientFactory.Instance;

	public static IClientFactory get() {
		return _factory;
	}

	/**
	 * 
	 * 注册服务工厂
	 * 
	 * @param factory
	 */
	public static void register(IClientFactory factory) {
		_factory = factory;
	}

}
