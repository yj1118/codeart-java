package apros.codeart.echo.rpc;

public interface IServer {
	/// <summary>
	/// 服务的名称
	/// </summary>
	String getName();

	void open();

	void close();
}
