package apros.codeart.ddd.cqrs.slave;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.message.DomainMessageHandler;
import apros.codeart.dto.DTObject;

public abstract class RemoteObjectHandler extends DomainMessageHandler {

	@Override
	public void process(String msgName, String msgId, DTObject content) {
		AppSession.setIdentity(content.getObject("identity")); // 先初始化身份
		handle(content);
	}

	protected abstract void handle(DTObject content);

//	#endregion

}
