package apros.codeart.rabbitmq.rpc;

import apros.codeart.echo.rpc.IRPCHandler;
import apros.codeart.echo.rpc.IServer;
import apros.codeart.rabbitmq.ConsumerCluster;

class RPCServerCluster extends ConsumerCluster<RPCServer> implements IServer {

	private final String _name;

	public String getName() {
		return _name;
	}

	public RPCServerCluster(String method, IRPCHandler handler) {
		super(RPCConfig.ServerPolicy, RPCConfig.getServerConfig(method), RPCConfig.getServerQueue(method),
				(cluster) -> {
					var rc = (RPCServerCluster) cluster;
					return new RPCServer(rc, cluster.queue(), handler);
				});
		_name = method;
	}
}
