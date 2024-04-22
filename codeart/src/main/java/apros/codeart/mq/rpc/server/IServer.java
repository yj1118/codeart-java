package apros.codeart.mq.rpc.server;

public interface IServer {
	/// <summary>
	/// 服务的名称
	/// </summary>
	String getName();

	void initialize(IRPCHandler handler);

	void open();

	void close();
}
