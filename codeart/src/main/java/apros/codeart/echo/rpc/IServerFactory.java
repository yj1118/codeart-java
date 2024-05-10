package apros.codeart.echo.rpc;

public interface IServerFactory {

	void register(String method, IRPCHandler handler);

	IServer get(String method);

	Iterable<IServer> getAll();
}
