package apros.codeart.mq.rpc.client;

import java.util.function.Consumer;

import apros.codeart.InterfaceImplementer;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.FactorySetting;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.rpc.MQRPC;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;

public class RPCClient {

	public static TransferData invoke(String method, Consumer<DTObject> fillArg) {
		var arg = DTObject.editable();
		fillArg.accept(arg);
		return invoke(method, arg);
	}

	public static TransferData invoke(String method, DTObject arg) {
		return _pool.using((client) -> {
			return client.invoke(method, arg);
		});
	}

//	 #region 获取客户端实例

	private static FactorySetting<IClientFactory> _setting = new FactorySetting<IClientFactory>(IClientFactory.class,
			() -> {
				InterfaceImplementer imp = MQRPC.getClientFactoryImplementer();
				if (imp != null) {
					return imp.getInstance(IClientFactory.class);
				}
				return null;
			});

	public static void register(IClientFactory factory) {
		_setting.register(factory);
	}

	private static Pool<IClient> _pool = new Pool<IClient>(IClient.class, new PoolConfig(10, 500), (isTempItem) -> {
		var factory = _setting.getFactory();
		return factory.create(ClientConfig.Instance);
	}, (client) -> {
		client.clear();
	});

//	#endregion

	/**
	 * 释放客户端资源
	 */
	static void cleanup() {
		_pool.dispose();
	}

}
