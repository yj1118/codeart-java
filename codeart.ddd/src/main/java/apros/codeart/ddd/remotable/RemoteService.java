package apros.codeart.ddd.remotable;

import apros.codeart.context.AppSession;
import apros.codeart.context.GlobalContext;
import apros.codeart.ddd.metadata.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.rpc.client.RPCClient;

public class RemoteService {

	public static DTObject getObject(Class<?> objectType, Object id) {
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
		var arg = createEventArg(objectType, id);
		var eventName = RemoteObjectUpdated.getEventName(remoteType);
		EventPortal.Publish(eventName, arg);
	}

	public static void NotifyDeleted(RemoteType remoteType, object id) {
		var arg = CreateEventArg(remoteType, id);
		var eventName = RemoteObjectDeleted.GetEventName(remoteType);
		EventPortal.Publish(eventName, arg);
	}

	private static DTObject createEventArg(Class<?> objectType, Object id) {
		var arg = DTObject.editable();
		arg["identity"] = AppSession.adaptIdentity();
		arg["typeName"] = remoteType.FullName;
		arg["id"] = id;
		return arg;
	}

	#

	region 初始化

	internal

	static void Initialize()
	 {
	     //开启获取远程对象的RPC服务
	     var tips = RemotableAttribute.GetTips();
	     foreach (var tip in tips)
	     {
	         var methodName = RemoteActionName.GetObject(tip.RemoteType);
	         RPCServer.Initialize(methodName, GetRemoteObject.Instance);
	     }

	     //订阅事件
	     SubscribeEvents();
	 }

	internal

	static void Cleanup()
	 {
	     var tips = RemotableAttribute.GetTips();
	     foreach (var tip in tips)
	     {
	         var methodName = RemoteActionName.GetObject(tip.RemoteType);
	         RPCServer.Close(methodName);
	     }

	     //取消订阅
	     CancelEvents();
	 }

	#region 订阅/取消订阅事件

	private static void SubscribeEvents()
	 {
	     var remoteTypes = RemoteType.GetTypes();
	     foreach (var remoteType in remoteTypes)
	     {
	         RemoteObjectUpdated.Subscribe(remoteType);
	         RemoteObjectDeleted.Subscribe(remoteType);
	     }
	 }

	/// <summary>
	/// 取消订阅
	/// </summary>
	private static void CancelEvents()
	 {
	     var remoteTypes = RemoteType.GetTypes();
	     foreach (var remoteType in remoteTypes)
	     {
	         //取消订阅对象被修改和删除的事件
	         RemoteObjectUpdated.Cancel(remoteType);
	         RemoteObjectDeleted.Cancel(remoteType);
	     }
	 }

	#endregion

	#endregion

}
