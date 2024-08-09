package apros.codeart.log;

import apros.codeart.dto.DTObject;

public final class Logger {
    private Logger() {
    }

    public static void write(DTObject content) {
        var code = content.getCode();
        FileLogger.Instance.info(code);
    }

    public static void fatal(Throwable ex) {
        FileLogger.Instance.error(ex);
    }
}
