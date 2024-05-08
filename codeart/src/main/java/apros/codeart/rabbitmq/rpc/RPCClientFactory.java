package apros.codeart.rabbitmq.rpc;

import apros.codeart.mq.rpc.client.ClientConfig;
import apros.codeart.mq.rpc.client.IClient;
import apros.codeart.mq.rpc.client.IClientFactory;

final class RPCClientFactory implements IClientFactory {
	public IClient create(ClientConfig config) {
		return new RPCClient(config.timeout());
	}

	public static final RPCClientFactory Instance = new RPCClientFactory();
}
