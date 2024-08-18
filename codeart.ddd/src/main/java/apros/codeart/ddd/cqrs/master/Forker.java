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
import apros.codeart.rabbitmq.rpc.RPCConfig;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public final class Forker {
    private Forker() {
    }

    private static final Function<String, Master> _findMaster = LazyIndexer.init((objTypeName) -> {
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

    private static final Function<String, DTObject> _getSchema = LazyIndexer.init((objTypeName) -> {
        return getSchema(objTypeName, null);
    });

    /**
     * 得到要远程输出的架构
     *
     * @param objectTypeName
     * @return
     */
    private static DTObject getSchema(String objectTypeName, Function<String, Boolean> filter) {
        var master = findMaster(objectTypeName, true);

        var schema = DTObject.editable();
        var meta = ObjectMetaLoader.get(objectTypeName);

        for (var member : master.members()) {

            if (filter != null && filter.apply(member) == false) continue;

            var prop = meta.findProperty(member);
            var monoMeta = prop.monoMeta();

            if (monoMeta == null) {

                if (prop.isCollection()) {
                    schema.push(member);
                    continue;
                }

                schema.setString(member, StringUtil.empty());
                continue;
            } else {

                var nextSchema = getSchema(monoMeta.name(), null);

                if (prop.isCollection()) {
                    schema.push(member, nextSchema);
                    continue;
                }

                schema.setObject(member, nextSchema);
                continue;
            }

        }

        return schema;
    }


    public static void notifyAdd(IAggregateRoot root) {
        if (!isEnabled(root))
            return;

        var objectType = root.getClass();
        var typeName = objectType.getSimpleName();
        var schema = _getSchema.apply(typeName);

        var data = DTOMapper.toDTO((DomainObject) root, schema);


        var content = DTObject.editable();
        content.setString("typeName", typeName);
        content.combineObject("data", data);

        var messageName = ActionName.objectAdded(objectType);
        DomainMessage.send(messageName, content);

    }

    public static void notifyUpdate(IAggregateRoot root) {
        if (!isEnabled(root))
            return;

        var objectType = root.getClass();
        var typeName = objectType.getSimpleName();
        var obj = (DomainObject) root;
        var schema = getSchema(typeName, obj::isPropertyChanged);

        var data = DTOMapper.toDTO((DomainObject) root, schema);

        if (data.isEmpty()) return;

        data.setValue("id", root.getIdentity());

        var content = DTObject.editable();
        content.setString("typeName", typeName);
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

        var messageName = ActionName.objectDeleted(objectType);
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
            // 注意，提供内部元数据服务的用的是持久队列
            // 这样就算master端后启动，slave先启动，slave也可以获得元数据
            RPCServer.register(ActionName.getObjectMeta(objectType), GetObjectMeta.Instance, RPCConfig.ServerPersistent);
        }
    }

    public static void dispose() {
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
