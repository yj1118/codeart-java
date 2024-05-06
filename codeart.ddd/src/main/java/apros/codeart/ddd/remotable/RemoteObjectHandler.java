package apros.codeart.ddd.remotable;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.message.DomainMessageHandler;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPriority;

public abstract class RemoteObjectHandler extends DomainMessageHandler {

	@Override
	public EventPriority getPriority() {
		return EventPriority.High;
	}

	@Override
	public void process(String msgName, String msgId, DTObject content) {
		AppSession.setIdentity(content.getObject("identity")); // 先初始化身份
		handle(content);
	}

	protected abstract void handle(DTObject content);

//	#endregion

}
