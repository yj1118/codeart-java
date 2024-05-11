package apros.codeart.rabbitmq.rpc;

import java.util.HashMap;

import apros.codeart.echo.rpc.IRPCHandler;
import apros.codeart.echo.rpc.IServer;
import apros.codeart.echo.rpc.IServerFactory;
import apros.codeart.rabbitmq.ConsumerClusterFactory;

public final class RabbitMQRPCServerFactory implements IServerFactory {

	public void register(String method, IRPCHandler handler) {
		var server = new RPCServerCluster(method, handler);
		_servers.put(method, server);
		ConsumerClusterFactory.add(server);
	}

	public IServer get(String method) {
		return _servers.get(method);
	}

	@Override
	public Iterable<IServer> getAll() {
		return _servers.values();
	}

	private static final HashMap<String, IServer> _servers = new HashMap<String, IServer>();

	public static final RabbitMQRPCServerFactory Instance = new RabbitMQRPCServerFactory();

}
