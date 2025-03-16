package apros.codeart.log;

import apros.codeart.AppConfig;
import apros.codeart.dto.DTObject;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.HashMap;

final class LogFilter {


    private LogFilter() {
    }

    private static final Iterable<String> _moduleNames;

    public static boolean enable(String moduleName) {
        if (moduleName.equals("*")) return true;
        if (_moduleNames == null) return false;
        return Iterables.contains(_moduleNames, moduleName);
    }

    static {
        _moduleNames = AppConfig.getStrings("log.@trace.filter", false);
    }

}
