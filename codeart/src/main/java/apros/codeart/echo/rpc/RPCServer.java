package apros.codeart.echo.rpc;

public final class RPCServer {
	private RPCServer() {
	}

	/**
	 * 注册服务，但不启动
	 * 
	 * @param method
	 * @param handler
	 */
	public static void register(String method, IRPCHandler handler) {
		_factory.register(method, handler);
	}

	/// <summary>
	/// 开启所有服务
	/// </summary>
	/// <param name="method"></param>
	static void open() {
		var servers = _factory.getAll();
		for (var server : servers) {
			server.open();
			RPCEvents.raiseServerOpened(server, new RPCEvents.ServerOpenedArgs(server.getName()));
		}
	}

	public static void close(String method) {
		var server = _factory.get(method);
		RPCEvents.raiseServerClosed(server, new RPCEvents.ServerClosedArgs(method));
	}

	private static IServerFactory _factory;

	public static void Register(IServerFactory factory) {
		_factory = factory;
	}

}
