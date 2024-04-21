package apros.codeart.mq.event;

import apros.codeart.mq.TransferData;

public class EventHandler implements IEventHandler {
	protected EventHandler() {
	}

	public void handle(String eventName, TransferData arg) {
	}

	public EventPriority getPriority() {
		return EventPriority.Medium;
	}

}