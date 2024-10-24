package apros.codeart.ddd.saga;

import apros.codeart.util.SafeAccess;
import apros.codeart.util.SafeAccessImpl;

public final class EventLogFactory {
    private EventLogFactory() {
    }

    private static IEventLogFactory _factory;

    private static volatile IEventLog _instance;

    private static final Object _lockObject = new Object();

    public static IEventLog createLog() {
        if (_instance == null) {
            synchronized (_lockObject) {
                if (_instance == null) {
                    _instance = _factory.create();
                    SafeAccessImpl.checkUp(_instance);
                }
            }
        }
        return _instance;
    }

    public static IEventLogFactory getFactory() {
        return _factory;
    }

    public static void register(IEventLogFactory factory) {
        _factory = factory;
    }

}
