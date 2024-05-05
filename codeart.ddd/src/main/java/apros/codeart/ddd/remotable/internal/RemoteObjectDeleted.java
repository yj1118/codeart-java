package apros.codeart.ddd.remotable.internal;

import apros.codeart.ddd.cqrs.master.RemoteActionName;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.ddd.remotable.RemoteObjectHandler;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

final class RemoteObjectDeleted {

	private RemoteObjectDeleted() {
	}

	public static String getMessageName(Class<?> objectType) {
		return RemoteActionName.objectDeleted(objectType);
	}

	public static void subscribe(Class<?> objectType) {
		var messageName = getMessageName(objectType);
		DomainMessage.subscribe(messageName, handler);
	}

	public static void cancel(Class<?> remoteType) {
		var messageName = getMessageName(remoteType);
		DomainMessage.cancel(messageName);
	}

	@SafeAccess
	private static class RemoteObjectDeletedHandler extends RemoteObjectHandler {

		@Override
		protected void handle(DTObject content) {
			useDefine(content, (define, id) -> {
				RemotePortal.deleteObject(define, id);
			});
		}
	}

	private static final RemoteObjectDeletedHandler handler = new RemoteObjectDeletedHandler();

}
