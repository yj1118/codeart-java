package apros.codeart.ddd.cqrs.slave;

import apros.codeart.ddd.cqrs.ActionName;
import apros.codeart.ddd.cqrs.CQRSConfig;
import apros.codeart.ddd.metadata.SchemeCode;
import apros.codeart.ddd.metadata.internal.MetadataLoader;
import apros.codeart.ddd.virtual.VirtualRoot;
import apros.codeart.ddd.virtual.internal.VirtualRepository;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.rpc.RPCClient;

public final class Brancher {
    private Brancher() {
    }

    public static void initialize() {
        loadRemoteObjectMeta();
        subscribeEvents();
    }

    private static void loadRemoteObjectMeta() {
        var slaves = CQRSConfig.slaves();
        if (slaves == null)
            return;
        for (var slave : slaves) {
            var scheme = getRemoteObjectMeta(slave.name());
            var dynamicType = SchemeCode.parse(scheme);
            MetadataLoader.register(dynamicType);
            System.out.println("Loaded remote object meta: " + slave.name());
        }
    }

    private static DTObject getRemoteObjectMeta(String name) {

        var methodName = ActionName.getObjectMeta(name);
        return RPCClient.invoke(methodName, (arg) -> {
            arg.setString("name", name);
        });
    }

    /**
     * 主动获取远程对象，获取后对象会被持久化在本地
     *
     * @param name 类型名称
     * @param id
     * @return
     */
    public static VirtualRoot getRemoteObject(String name, Object id) {

        var methodName = ActionName.getObject(name);
        var data = RPCClient.invoke(methodName, (arg) -> {
            arg.setString("name", name);
            arg.setValue("id", id);
        });

        if (data.isEmpty()) return VirtualRoot.empty();

        return VirtualRepository.add(name, data);
    }

    private static void subscribeEvents() {
        var slaves = CQRSConfig.slaves();
        if (slaves == null)
            return;
        for (var slave : slaves) {
            RemoteObjectAdded.subscribe(slave.name());
            RemoteObjectUpdated.subscribe(slave.name());
            RemoteObjectDeleted.subscribe(slave.name());
        }
    }

    /**
     * 取消订阅
     */
    private static void cancelEvents() {
        var slaves = CQRSConfig.slaves();
        if (slaves == null) return;
        for (var slave : slaves) {
            RemoteObjectAdded.cancel(slave.name());
            RemoteObjectUpdated.cancel(slave.name());
            RemoteObjectDeleted.cancel(slave.name());
        }
    }

    public static void dispose() {
        cancelEvents();
    }

}