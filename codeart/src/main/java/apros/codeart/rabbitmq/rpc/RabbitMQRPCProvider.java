package apros.codeart.rabbitmq.rpc;

import apros.codeart.IModuleProvider;
import apros.codeart.util.SafeAccess;

@SafeAccess
public final class RabbitMQRPCProvider implements IModuleProvider {

	@Override
	public String name() {
		return "rabbitMQ-rpc";
	}

	@Override
	public void setup() {
		apros.codeart.echo.rpc.RPCClientFactory.register(RPCClientFactory.Instance);
		apros.codeart.echo.rpc.RPCServerFactory.register(RPCServerFactory.Instance);
	}
}
