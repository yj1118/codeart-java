package apros.codeart.rabbitmq.rpc;

import apros.codeart.mq.rpc.server.IRPCHandler;
import apros.codeart.mq.rpc.server.IServer;
import apros.codeart.rabbitmq.internal.ConsumerCluster;

class RPCServerCluster extends ConsumerCluster<RPCServer> implements IServer {

	private IRPCHandler _handler;

	public IRPCHandler handler() {
		return _handler;
	}

	private final String _name;

	public String getName() {
		return _name;
	}

	public RPCServerCluster(String method) {
		super(RPCConfig.ServerPolicy, RPCConfig.getServerConfig(method), RPCConfig.getServerQueue(method),
				(cluster) -> {
					var rc = (RPCServerCluster) cluster;
					return new RPCServer(rc, cluster.queue(), rc.handler());
				});
		_name = method;
	}

	public void initialize(IRPCHandler handler) {
		_handler = handler;
	}
}
