package apros.codeart.mq.rpc.client;

public interface IClientFactory {
	IClient create(ClientConfig config);
}
