package apros.codeart.ddd.cqrs.slave;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.cqrs.ActionName;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Repository;
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

		@Override
		protected void handle(DTObject content) {

			var typeName = content.getString("typeName");
			var id = content.getValue("id");

			var repoitory = Repository.create(typeName);

			DataContext.using(() -> {
				var obj = repoitory.findRoot(id, QueryLevel.Single);
				repoitory.deleteRoot(obj);
			});

		}
	}

	private static final RemoteObjectDeletedHandler handler = new RemoteObjectDeletedHandler();
}
