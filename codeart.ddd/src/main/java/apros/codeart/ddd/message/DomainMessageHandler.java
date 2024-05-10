package apros.codeart.ddd.message;

import apros.codeart.dto.DTObject;
import apros.codeart.echo.event.IEventHandler;

public abstract class DomainMessageHandler implements IEventHandler {

	@Override
	public void handle(String eventName, DTObject data) {

//		if (!info.exist(DomainMessagePublisher.headerType))
//			return;

		var msgId = data.getString("id");

		// 消息幂等性判断,todo...

		var content = data.getObject("body");

		process(eventName, msgId, content);
	}

	protected abstract void process(String msgName, String msgId, DTObject content);

}
