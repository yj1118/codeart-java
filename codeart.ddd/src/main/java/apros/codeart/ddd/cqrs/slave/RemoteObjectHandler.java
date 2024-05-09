package apros.codeart.ddd.cqrs.slave;

import apros.codeart.ddd.message.DomainMessageHandler;
import apros.codeart.dto.DTObject;

public abstract class RemoteObjectHandler extends DomainMessageHandler {

	@Override
	public void process(String msgName, String msgId, DTObject content) {
		handle(content);
	}

	protected abstract void handle(DTObject content);

//	#endregion

}
