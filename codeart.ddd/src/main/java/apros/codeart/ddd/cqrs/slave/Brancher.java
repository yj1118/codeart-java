package apros.codeart.ddd.cqrs.slave;

import apros.codeart.ddd.cqrs.ActionName;
import apros.codeart.ddd.cqrs.CQRSConfig;
import apros.codeart.ddd.metadata.SchemeCode;
import apros.codeart.ddd.metadata.internal.MetadataLoader;
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
        }
    }

    private static DTObject getRemoteObjectMeta(String name) {

        var methodName = ActionName.getObjectMeta(name);
        return RPCClient.invoke(methodName, (arg) -> {
            arg.setString("name", name);
        });
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