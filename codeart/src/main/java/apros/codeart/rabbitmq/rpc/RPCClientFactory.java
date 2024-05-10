package apros.codeart.rabbitmq.rpc;

import apros.codeart.echo.rpc.ClientConfig;
import apros.codeart.echo.rpc.IClient;
import apros.codeart.echo.rpc.IClientFactory;

public final class RPCClientFactory implements IClientFactory {
	public IClient create(ClientConfig config) {
		return new RPCClient(config.timeout());
	}

	public static final RPCClientFactory Instance = new RPCClientFactory();
}
