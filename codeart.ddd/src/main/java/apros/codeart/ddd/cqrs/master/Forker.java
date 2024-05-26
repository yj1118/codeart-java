package apros.codeart.ddd.cqrs.master;

import java.util.function.Function;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.cqrs.ActionName;
import apros.codeart.ddd.cqrs.CQRSConfig;
import apros.codeart.ddd.internal.DTOMapper;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.rpc.RPCServer;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;

public final class Forker {
	private Forker() {
	}

	private static Function<String, Master> _findMaster = LazyIndexer.init((objTypeName) -> {
		return ListUtil.find(CQRSConfig.masters(), (m) -> {
			return m.name().equalsIgnoreCase(objTypeName);
		});
	});

	public static Master findMaster(String objTypeName, boolean throwError) {
		var master = _findMaster.apply(objTypeName);
		if (master == null && throwError) {
			throw new IllegalStateException(Language.strings("apros.codeart.ddd", "NoMaster", objTypeName));
		}

		return master;
	}

	private static Master findMaster(Object obj, boolean throwError) {
		var aggregate = obj.getClass().getSimpleName();
		return findMaster(aggregate, throwError);
	}

	public static boolean isEnabled(Object obj) {
		return findMaster(obj, false) != null;
	}

	public static boolean isEnabled(String objTypeName) {
		return findMaster(objTypeName, false) != null;
	}

	public static void notifyAdd(IAggregateRoot root) {
		if (!isEnabled(root))
			return;

		var data = DTOMapper.toDTO((DomainObject) root, (obj) -> {
			var master = findMaster(obj, true);
			return master.members();
		});

		var objectType = root.getClass();
		var content = DTObject.editable();
		content.setString("typeName", objectType.getSimpleName());
		content.combineObject("data", data);

		var messageName = ActionName.objectAdded(objectType);
		DomainMessage.send(messageName, content);

	}

	public static void notifyUpdate(IAggregateRoot root) {
		if (!isEnabled(root))
			return;

		var data = DTOMapper.toDTO((DomainObject) root, (obj) -> {
			var master = findMaster(obj, true);
			if (obj == root) {
				// 只收集更改了的属性
				return ListUtil.filter(master.members(), (member) -> {
					return obj.isPropertyChanged(member);
				});
			}

			return master.members();
		});

		var objectType = root.getClass();
		var content = DTObject.editable();
		content.setString("typeName", objectType.getSimpleName());
		content.combineObject("data", data);

		var messageName = ActionName.objectUpdated(objectType);
		DomainMessage.send(messageName, content);
	}

	public static void notifyDelete(IAggregateRoot root) {
		if (!isEnabled(root))
			return;

		var id = root.getIdentity();

		var objectType = root.getClass();
		var content = DTObject.editable();
		content.setString("typeName", objectType.getSimpleName());
		content.setValue("id", id);

		var messageName = ActionName.objectUpdated(objectType);
		DomainMessage.send(messageName, content);
	}

	public static void initialize() {
		// 开启获取远程对象的元数据的的rpc服务
		var masters = CQRSConfig.masters();

		if (masters == null)
			return;

		for (var master : masters) {
			// 虽然可以直接用名称，但是需要通过get验证下
			var objectType = ObjectMetaLoader.get(master.name()).objectType();
			RPCServer.register(ActionName.getObjectMeta(objectType), GetObjectMeta.Instance);
		}
	}

	public static void cleanup() {
		var masters = CQRSConfig.masters();

		if (masters == null)
			return;

		for (var master : masters) {
			RPCServer.close(ActionName.getObjectMeta(master.name()));
		}
	}

//	/**
//	 * 
//	 * 分发以执行sql为基础的数据构成
//	 * 
//	 * @param aggregate 聚合，slave可以根据聚合来订阅数据，一般聚合就是一个内聚根的类名
//	 * @param sql
//	 * @param data
//	 */
//	public static void dispatch(String aggregate, String sql, MapData data) {
//		if (!isEnabled(aggregate))
//			return;
//
//		DTObject content = DTObject.editable();
//		content.setByte("type", ForkType.DB.getValue());
//		content.setString("agg", aggregate);
//		content.setString("sql", sql);
//		if (data != null) {
//			content.combineObject("data", data.asDTO());
//		}
//
//		var eventName = getEventName(aggregate);
//		DomainMessage.send(eventName, content);
//	}
//
//	public static void subscribe(String aggregate) {
//		var eventName = getEventName(aggregate);
//		DomainMessage.subscribe(eventName, ReceiveChangedHandler.instance);
//	}
//
//	private static Function<String, String> _getEventName = LazyIndexer.init((aggregate) -> {
//		return String.format("d:cqrs-fork-%s", aggregate);
//	});
//
//	public static String getEventName(String aggregate) {
//		return _getEventName.apply(aggregate);
//	}

}
