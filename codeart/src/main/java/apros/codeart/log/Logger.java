package apros.codeart.log;

import apros.codeart.AppConfig;
import apros.codeart.UIException;
import apros.codeart.dto.DTObject;
import apros.codeart.runtime.TypeUtil;

public final class Logger {


    private Logger() {
    }

    public static void error(Throwable ex) {
        if (TypeUtil.is(ex, UIException.class)) return; // UI错误，不记日志
        FileLogger.INSTANCE.error(ex);
    }

    public static void trace(DTObject content) {
        TRACE_LOGGER.trace(content);
    }

    public static void trace(String message) {
        TRACE_LOGGER.trace(message);
    }

    public static void trace(String formatMessage, Object... args) {
        TRACE_LOGGER.trace(formatMessage, args);
    }

    private final static ILogger TRACE_LOGGER;

    static {
        boolean traceEnabled = AppConfig.getBoolean("log.trace", false);
        TRACE_LOGGER = traceEnabled ? FileLogger.INSTANCE : NoneLogger.INSTANCE;
    }

}
