package apros.codeart.ddd.saga;

import apros.codeart.AppConfig;
import apros.codeart.TestSupport;
import apros.codeart.dto.DTObject;

import java.util.List;

public final class SAGAConfig {

    private SAGAConfig() {

    }

    public static void load(DTObject root) {
        if (root == null) return;
        _retainDays = root.getInt("@log.retain", 0); // 默认永久保留
        _eventTimeout = root.getInt("@event.timeout", 60); //领域事件默认超时时间60秒
        _specifiedEvents = root.getStrings("@event.specified", false);
    }

    private static int _retainDays;

    public static int retainDays() {
        return _retainDays;
    }

    private static int _eventTimeout;

    public static int eventTimeout() {
        return _eventTimeout;
    }


    private static Iterable<String> _specifiedEvents;

    public static Iterable<String> specifiedEvents() {
        return _specifiedEvents;
    }

    private static DTObject _section;

    public static DTObject section() {
        return _section;
    }

    static {

        _section = AppConfig.section("saga");
        if (_section != null) {
            load(_section);
        } else {
            _section = DTObject.Empty;
            _retainDays = 0;
        }

    }
}
