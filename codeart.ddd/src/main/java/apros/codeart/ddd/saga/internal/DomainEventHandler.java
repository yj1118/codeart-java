package apros.codeart.ddd.saga.internal;

import apros.codeart.context.AppSession;
import apros.codeart.dto.DTObject;
import apros.codeart.log.Logger;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.event.EventPriority;
import apros.codeart.mq.event.IEventHandler;

public abstract class DomainEventHandler implements IEventHandler {

	public EventPriority getPriority() {
		return EventPriority.Medium;
	}

	public void handle(String eventName, TransferData data) {
		try {
			initIdentity(data.info());
			handle(data.info());
		} catch (Exception ex) {
			Logger.fatal(ex);
		}
	}

	protected abstract void handle(DTObject arg);

	private void initIdentity(DTObject arg) {
		var identity = arg.getObject("identity");
		AppSession.setIdentity(identity);
	}
}
