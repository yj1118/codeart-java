package apros.codeart.ddd.saga.internal;

import apros.codeart.dto.DTObject;
import apros.codeart.log.Logger;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.event.IEventHandler;

public abstract class DomainEventHandler implements IEventHandler {

	public void handle(String eventName, TransferData data) {
		// 时间订阅器已经添加了appSession，所以这里不需要再加了
		try {
			handle(data.info());
		} catch (Exception ex) {
			Logger.fatal(ex);
		}
	}

	protected abstract void handle(DTObject arg);
}
