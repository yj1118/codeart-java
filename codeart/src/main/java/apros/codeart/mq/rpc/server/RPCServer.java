package apros.codeart.mq.rpc.server;

import apros.codeart.InterfaceImplementer;
import apros.codeart.mq.FactorySetting;
import apros.codeart.mq.rpc.MQRPC;

public final class RPCServer {
	private RPCServer() {
	}

	/**
	 * 初始化服务，但不启动
	 * 
	 * @param method
	 * @param handler
	 */
	public static void initialize(String method, IRPCHandler handler) {
		var server = _setting.getFactory().create(method);
		server.initialize(handler);
		// RPCEvents.RaiseServerOpened(server, new RPCEvents.ServerOpenedArgs(method));
		// //这里不是开启，不需要通知事件
	}

	/// <summary>
	/// 开启所有服务
	/// </summary>
	/// <param name="method"></param>
	static void open() {
		var servers = _setting.getFactory().getAll();
		for (var server : servers) {
			server.open();
			RPCEvents.raiseServerOpened(server, new RPCEvents.ServerOpenedArgs(server.getName()));
		}
	}

	public static void close(String method) {
		var server = _setting.getFactory().create(method);
		server.close();
		RPCEvents.raiseServerClosed(server, new RPCEvents.ServerClosedArgs(method));
	}

	private static FactorySetting<IServerFactory> _setting = new FactorySetting<IServerFactory>(IServerFactory.class,
			() -> {

				InterfaceImplementer imp = MQRPC.getServerFactoryImplementer();
				if (imp != null) {
					return imp.getInstance(IServerFactory.class);
				}
				return null;
			});

	public static void Register(IServerFactory factory) {
		_setting.register(factory);
	}

}
