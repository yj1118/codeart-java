package apros.codeart.rabbitmq.rpc;

import apros.codeart.echo.rpc.IRPCHandler;
import apros.codeart.echo.rpc.IServer;
import apros.codeart.rabbitmq.ConsumerCluster;
import apros.codeart.rabbitmq.Policy;

class RPCServerCluster extends ConsumerCluster<RPCServer> implements IServer {

    private final String _name;

    public String getName() {
        return _name;
    }


    public RPCServerCluster(String method, IRPCHandler handler, Policy policy) {
        super(policy, RPCConfig.getServerConfig(method), RPCConfig.getServerQueue(method),
                (cluster) -> {
                    var rc = (RPCServerCluster) cluster;
                    return new RPCServer(rc, cluster.queue(), handler, policy);
                });
        _name = method;
    }
}
