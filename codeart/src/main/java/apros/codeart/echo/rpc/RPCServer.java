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
		RPCServerFactory.get().register(method, handler);
	}

	/// <summary>
	/// 开启所有服务
	/// </summary>
	/// <param name="method"></param>
	static void open() {
		var servers = RPCServerFactory.get().getAll();
		for (var server : servers) {
			server.open();
			RPCEvents.raiseServerOpened(server, new RPCEvents.ServerOpenedArgs(server.getName()));
		}
	}

	public static void close(String method) {
		var server = RPCServerFactory.get().get(method);
		RPCEvents.raiseServerClosed(server, new RPCEvents.ServerClosedArgs(method));
	}

}
