package apros.codeart.ddd.message;

import apros.codeart.AppConfig;
import apros.codeart.dto.DTObject;

public class MessageConfig {
    private MessageConfig() {

    }

    private static DTObject _section;

    public static DTObject section() {
        return _section;
    }

    static {

        _section = AppConfig.section("message");
        if (_section == null)
            _section = DTObject.empty();
    }
}
