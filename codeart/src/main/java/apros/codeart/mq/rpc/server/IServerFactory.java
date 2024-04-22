package apros.codeart.mq.rpc.server;

public interface IServerFactory {
	IServer create(String method);

	Iterable<IServer> getAll();
}
