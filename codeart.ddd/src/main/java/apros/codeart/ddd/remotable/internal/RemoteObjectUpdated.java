package apros.codeart.ddd.remotable.internal;

import apros.codeart.ddd.remotable.RemoteObjectHandler;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPortal;
import apros.codeart.util.SafeAccess;

class RemoteObjectUpdated {

	public static String getEventName(Class<?> remoteType) {
		return RemoteActionName.objectUpdated(remoteType);
	}

	public static void subscribe(Class<?> remoteType) {
		var eventName = getEventName(remoteType);
		EventPortal.subscribe(eventName, handler);
	}

	public static void cancel(Class<?> remoteType) {
		var eventName = getEventName(remoteType);
		EventPortal.cancel(eventName);
	}

	@SafeAccess
	private static class RemoteObjectUpdatedHandler extends RemoteObjectHandler {

		@Override
		protected void handle(DTObject arg) {
			useDefine(arg, (rooType, id) -> {
				RemotePortal.updateObject(rooType, id);
			});
		}
	}

	private static final RemoteObjectUpdatedHandler handler = new RemoteObjectUpdatedHandler();
}
