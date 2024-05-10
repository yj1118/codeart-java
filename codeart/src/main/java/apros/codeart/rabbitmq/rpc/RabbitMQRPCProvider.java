package apros.codeart.rabbitmq.rpc;

import apros.codeart.IModuleProvider;
import apros.codeart.echo.rpc.RPCClient;
import apros.codeart.echo.rpc.RPCServer;
import apros.codeart.util.SafeAccess;

@SafeAccess
public final class RabbitMQRPCProvider implements IModuleProvider {

	@Override
	public String name() {
		return "rabbitMQ-rpc";
	}

	@Override
	public void setup() {
		RPCClient.register(RPCClientFactory.Instance);
		RPCServer.Register(RPCServerFactory.Instance);
	}
}
