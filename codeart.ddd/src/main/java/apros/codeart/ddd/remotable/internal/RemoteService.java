package apros.codeart.ddd.remotable.internal;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.ddd.metadata.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.rpc.client.RPCClient;
import apros.codeart.mq.rpc.server.RPCServer;

public class RemoteService {

	public static DTObject getObject(Class<? extends DynamicRoot> objectType, Object id) {
		var methodName = RemoteActionName.getObject(objectType);
		var meta = ObjectMetaLoader.get(objectType);
		return RPCClient.invoke(methodName, (arg) -> {
			arg.setValue("id", id);
			arg.setString("typeName", meta.name());
			arg.setString("schemaCode", meta.schemeCode());
			arg.setObject("identity", AppSession.adaptIdentity());
		}).info();
	}

	public static void notifyUpdated(Class<?> objectType, Object id) {
		var content = createEventArg(objectType, id);
		var messageName = RemoteObjectUpdated.getMessageName(objectType);
		DomainMessage.send(messageName, content);
	}

	public static void notifyDeleted(Class<?> objectType, Object id) {
		var content = createEventArg(objectType, id);
		var messageName = RemoteObjectDeleted.getMessageName(objectType);
		DomainMessage.send(messageName, content);
	}

	private static DTObject createEventArg(Class<?> objectType, Object id) {
		var arg = DTObject.editable();
		arg.setObject("identity", AppSession.adaptIdentity());
		arg.setString("typeName", objectType.getSimpleName());
		arg.setValue("id", id);
		return arg;
	}
//
//	#
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
