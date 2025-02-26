package apros.codeart.log;

import apros.codeart.AppConfig;
import apros.codeart.UIException;
import apros.codeart.dto.DTObject;
import apros.codeart.runtime.TypeUtil;

public final class NoneLogger implements ILogger {


    private NoneLogger() {
    }


    @Override
    public void error(Throwable ex) {

    }

    @Override
    public void trace(DTObject content) {

    }

    @Override
    public void trace(String message) {

    }

    @Override
    public void trace(String formatMessage, Object... args) {
        
    }

    static final NoneLogger INSTANCE = new NoneLogger();

}
