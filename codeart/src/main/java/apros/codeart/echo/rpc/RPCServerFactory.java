package apros.codeart.echo.rpc;

import apros.codeart.rabbitmq.rpc.RabbitMQRPCServerFactory;

public final class RPCServerFactory {
	private RPCServerFactory() {
	}

	private static IServerFactory _factory = RabbitMQRPCServerFactory.Instance;

	public static IServerFactory get() {
		return _factory;
	}

	/**
	 * 
	 * 注册服务工厂
	 * 
	 * @param factory
	 */
	public static void register(IServerFactory factory) {
		_factory = factory;
	}

}
