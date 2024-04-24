package apros.codeart.ddd.remotable.internal;

import apros.codeart.ddd.remotable.RemoteObjectHandler;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPortal;
import apros.codeart.util.SafeAccess;

final class RemoteObjectDeleted {

	private RemoteObjectDeleted() {
	}

	public static String getEventName(Class<?> objectType) {
		return RemoteActionName.objectDeleted(objectType);
	}

	public static void subscribe(Class<?> objectType) {
		var eventName = getEventName(objectType);
		EventPortal.subscribe(eventName, handler);
	}

	public static void cancel(Class<?> remoteType) {
		var eventName = getEventName(remoteType);
		EventPortal.cancel(eventName);
	}

	@SafeAccess
	private static class RemoteObjectDeletedHandler extends RemoteObjectHandler {

		@Override
		protected void handle(DTObject arg) {
			useDefine(arg, (define, id) -> {
				RemotePortal.deleteObject(define, id);
			});
		}
	}

	private static final RemoteObjectDeletedHandler handler = new RemoteObjectDeletedHandler();

}
