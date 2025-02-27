package saga;

import apros.codeart.ddd.launcher.DomainServer;
import apros.codeart.dto.DTObject;
import subsystem.saga.*;

import java.util.ArrayList;

public final class Servers {

    private Servers() {
    }

    private static final ArrayList<DomainServer> _servers = new ArrayList<>();

    private static void init(int count) {
        _servers.clear();
        for (int i = 0; i < count; i++) {
            var index = i + 1;
            var serverName = String.format("server%d", index);
            var eventName = Common.getEventName(index);

            DTObject config = DTObject.editable();
            config.pushString("domainEvents", eventName);
            DomainServer server = new DomainServer(serverName, config);
            _servers.add(server);
        }
    }

    public static void open(int count) {
        init(count);

        for (var server : _servers) {
            server.open();
        }
    }

    public static void close() {
        for (var server : _servers) {
            server.close();
        }
        _servers.clear();
    }
}
