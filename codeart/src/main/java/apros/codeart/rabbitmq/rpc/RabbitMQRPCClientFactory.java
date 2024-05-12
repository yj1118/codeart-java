package apros.codeart.rabbitmq.rpc;

import apros.codeart.echo.rpc.ClientConfig;
import apros.codeart.echo.rpc.IClient;
import apros.codeart.echo.rpc.IClientFactory;

public final class RabbitMQRPCClientFactory implements IClientFactory {

	private RabbitMQRPCClientFactory() {
	}

	public IClient create(ClientConfig config) {
		return new RPCClient(config.timeout());
	}

	public static final RabbitMQRPCClientFactory Instance = new RabbitMQRPCClientFactory();
}
