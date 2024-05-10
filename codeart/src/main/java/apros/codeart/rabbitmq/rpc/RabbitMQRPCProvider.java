package apros.codeart.rabbitmq.rpc;

import apros.codeart.echo.IEchoProvider;
import apros.codeart.echo.rpc.RPCClient;
import apros.codeart.echo.rpc.RPCServer;
import apros.codeart.util.SafeAccess;

@SafeAccess
public final class RabbitMQRPCProvider implements IEchoProvider {

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
