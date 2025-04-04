package apros.codeart.log;

import apros.codeart.AppConfig;
import apros.codeart.UIException;
import apros.codeart.dto.DTObject;
import apros.codeart.runtime.TypeUtil;

public final class Logger {


    private Logger() {
    }

    public static void error(Throwable ex) {
        FileLogger.INSTANCE.error(ex);
    }

    public static void trace(String moduleName, DTObject content) {
        TRACE_LOGGER.trace(moduleName, content);
    }

    public static void trace(String moduleName, String message) {
        TRACE_LOGGER.trace(moduleName, message);
    }

    public static void trace(String moduleName, String formatMessage, Object... args) {
        TRACE_LOGGER.trace(moduleName, formatMessage, args);
    }

    private final static ILogger TRACE_LOGGER;

    static {
        boolean traceEnabled = AppConfig.getBoolean("log.trace", false);
        TRACE_LOGGER = traceEnabled ? FileLogger.INSTANCE : NoneLogger.INSTANCE;
    }

}
