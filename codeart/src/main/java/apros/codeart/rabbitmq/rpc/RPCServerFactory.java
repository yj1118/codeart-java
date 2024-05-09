package apros.codeart.rabbitmq.rpc;

import java.util.ArrayList;
import java.util.function.Function;

import apros.codeart.mq.rpc.server.IServer;
import apros.codeart.mq.rpc.server.IServerFactory;
import apros.codeart.rabbitmq.internal.ConsumerClusterFactory;
import apros.codeart.util.LazyIndexer;

final class RPCServerFactory implements IServerFactory {

	public IServer create(String method) {
		return _getServer.apply(method);
	}

	@Override
	public Iterable<IServer> getAll() {
		return _servers;
	}

	private static ArrayList<IServer> _servers = new ArrayList<IServer>();

	private static Function<String, IServer> _getServer = LazyIndexer.init((method) -> {
		var server = new RPCServerCluster(method);
		_servers.add(server);

		ConsumerClusterFactory.add(server);

		return server;
	});

	public static final RPCServerFactory Instance = new RPCServerFactory();

}
