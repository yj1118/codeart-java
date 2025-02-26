package apros.codeart.log;

import apros.codeart.UIException;
import apros.codeart.dto.DTObject;
import apros.codeart.runtime.TypeUtil;

public interface ILogger {

    void error(Throwable ex);

    void trace(DTObject content);

    void trace(String message);

    void trace(String formatMessage, Object... args);
}
