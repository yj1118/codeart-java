package apros.codeart.ddd.message;

import apros.codeart.ddd.message.internal.DomainMessagePublisher;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.event.EventPriority;
import apros.codeart.mq.event.IEventHandler;

public abstract class DomainMessageHandler implements IEventHandler {

	@Override
	public EventPriority getPriority() {
		return EventPriority.Medium;
	}

	@Override
	public void handle(String eventName, TransferData data) {
		var info = data.info();

		if (!info.exist(DomainMessagePublisher.headerType))
			return;

		var msgId = info.getString("id");
		var content = info.getObject("body");

		process(eventName, msgId, content);
	}

	protected abstract void process(String msgName, String msgId, DTObject content);

}
