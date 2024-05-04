package apros.codeart.ddd.cqrs.internal;

import apros.codeart.ddd.remotable.internal.RemotableImpl;
import apros.codeart.mq.rpc.server.RPCServer;

public class RemoteService {

//
//	region 初始化

	public static void initialize() {
		// 开启获取远程对象的RPC服务
		var tips = RemotableImpl.getTips();
		for (var tip : tips) {
			var methodName = RemoteActionName.getObject(tip.objectType());
			RPCServer.initialize(methodName, GetRemoteObject.instance);
		}

		// 订阅事件
		subscribeEvents();
	}

	public static void cleanup() {
		var tips = RemotableImpl.getTips();
		for (var tip : tips) {

			var methodName = RemoteActionName.getObject(tip.objectType());
			RPCServer.close(methodName);
		}

		// 取消订阅
		cancelEvents();
	}

//	#region 订阅/取消订阅事件

	private static void subscribeEvents() {
		var tips = RemotableImpl.getTips();
		for (var tip : tips) {
			RemoteObjectUpdated.subscribe(tip.objectType());
			RemoteObjectDeleted.subscribe(tip.objectType());
		}
	}

	/// <summary>
	/// 取消订阅
	/// </summary>
	private static void cancelEvents() {
		var tips = RemotableImpl.getTips();
		for (var tip : tips) {
			// 取消订阅对象被修改和删除的事件
			RemoteObjectUpdated.cancel(tip.objectType());
			RemoteObjectDeleted.cancel(tip.objectType());
		}
	}
//
//	#endregion
//
//	#endregion

}
