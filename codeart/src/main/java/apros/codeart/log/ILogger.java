package apros.codeart.log;

import apros.codeart.UIException;
import apros.codeart.dto.DTObject;
import apros.codeart.runtime.TypeUtil;

public interface ILogger {

    void error(Throwable ex);

    void trace(String moduleName, DTObject content);

    void trace(String moduleName, String message);

    void trace(String moduleName, String formatMessage, Object... args);
}
