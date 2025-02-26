package apros.codeart.ddd.saga.internal;

import apros.codeart.dto.DTObject;
import apros.codeart.echo.event.IEventHandler;
import apros.codeart.log.Logger;

public abstract class DomainEventHandler implements IEventHandler {

    public void handle(String eventName, DTObject data) {
        // 时间订阅器已经添加了appSession，所以这里不需要再加了
        try {
            handle(data);
        } catch (Throwable ex) {
            Logger.error(ex);
        }
    }

    protected abstract void handle(DTObject arg);
}
