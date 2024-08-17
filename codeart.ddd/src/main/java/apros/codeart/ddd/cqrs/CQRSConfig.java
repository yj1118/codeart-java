package apros.codeart.ddd.cqrs;

import java.util.ArrayList;

import apros.codeart.AppConfig;
import apros.codeart.ddd.cqrs.master.Master;
import apros.codeart.ddd.cqrs.slave.Slave;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;

public final class CQRSConfig {

    private CQRSConfig() {

    }

    private static ArrayList<Master> _masters;

    public static Iterable<Master> masters() {
        return _masters;
    }

    private static void init(DTObject config) {
        loadMaster(config);
        loadSlaves(config);
    }

    @SuppressWarnings("unchecked")
    private static void loadMaster(DTObject config) {
        _masters = new ArrayList<Master>();

        config.each("master", (name, value) -> {
            var members = ListUtil.map((Iterable<DTObject>) value, (t) -> t.getString());
            _masters.add(new Master(name, members));
        });
    }

    private static ArrayList<Slave> _slaves;

    public static Iterable<Slave> slaves() {
        return _slaves;
    }

    private static void loadSlaves(DTObject config) {
        var slaves = config.getStrings("slave", false);
        if (slaves == null) return;
        _slaves = ListUtil.map(slaves, (name) -> new Slave(name));
    }

    private static DTObject _section;

    public static DTObject section() {
        return _section;
    }

    static {

        _section = AppConfig.section("cqrs");
        if (_section != null) {
            init(_section);
        } else {
            _section = DTObject.Empty;
            _masters = null;
            _slaves = null;
        }

    }
}
