package apros.codeart.ddd.cqrs.slave;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.cqrs.ActionName;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

class RemoteObjectDeleted {

	public static void subscribe(String typeName) {
		var messageName = ActionName.objectDeleted(typeName);
		DomainMessage.subscribe(messageName, handler);
	}

	public static void cancel(String typeName) {
		var messageName = ActionName.objectUpdated(typeName);
		DomainMessage.cancel(messageName);
	}

	@SafeAccess
	private static class RemoteObjectDeletedHandler extends RemoteObjectHandler {

		@SuppressWarnings("unchecked")
		@Override
		protected void handle(DTObject content) {

			var typeName = content.getString("typeName");
			var id = content.getValue("id");
			var domainType = (Class<? extends IDomainObject>) ObjectMetaLoader.get(typeName).objectType();

			DataContext.using(() -> {
				var obj = (DomainObject) DataPortal.querySingle(domainType, id, QueryLevel.Single);
				DataPortal.delete(obj);
			});

		}
	}

	private static final RemoteObjectDeletedHandler handler = new RemoteObjectDeletedHandler();
}
