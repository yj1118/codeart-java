package apros.codeart.echo.rpc;

public interface IClientFactory {
	IClient create(ClientConfig config);
}
