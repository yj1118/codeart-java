package apros.codeart.mq.rpc;

public interface IClientFactory {
	IClient create(ClientConfig config);
}
