package apros.codeart.ddd.cqrs.slave;

import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.ddd.remotable.RemoteObjectHandler;
import apros.codeart.ddd.remotable.internal.RemotePortal;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

class RemoteObjectUpdated {

	public static String getMessageName(Class<?> remoteType) {
		return RemoteActionName.objectUpdated(remoteType);
	}

	public static void subscribe(Class<?> remoteType) {
		var messageName = getMessageName(remoteType);
		DomainMessage.subscribe(messageName, handler);
	}

	public static void cancel(Class<?> remoteType) {
		var messageName = getMessageName(remoteType);
		DomainMessage.cancel(messageName);
	}

	@SafeAccess
	private static class RemoteObjectUpdatedHandler extends RemoteObjectHandler {

		@Override
		protected void handle(DTObject content) {
			useDefine(content, (rooType, id) -> {
				RemotePortal.updateObject(rooType, id);
			});
		}
	}

	private static final RemoteObjectUpdatedHandler handler = new RemoteObjectUpdatedHandler();
}
