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

class RemoteObjectUpdated {

	public static void subscribe(String typeName) {
		var messageName = ActionName.objectUpdated(typeName);
		DomainMessage.subscribe(messageName, handler);
	}

	public static void cancel(String typeName) {
		var messageName = ActionName.objectUpdated(typeName);
		DomainMessage.cancel(messageName);
	}

	@SafeAccess
	private static class RemoteObjectUpdatedHandler extends RemoteObjectHandler {

		@SuppressWarnings("unchecked")
		@Override
		protected void handle(DTObject content) {

			var typeName = content.getString("typeName");
			var data = content.getObject("data");
			var domainType = (Class<? extends IDomainObject>) ObjectMetaLoader.get(typeName).objectType();

			DataContext.using(() -> {

				var id = data.getValue("id");
				var obj = (DomainObject) DataPortal.querySingle(domainType, id, QueryLevel.Single);

				// 加载数据，并标记为已改变
				obj.load(data, true);

				DataPortal.update(obj);
			});

		}
	}

	private static final RemoteObjectUpdatedHandler handler = new RemoteObjectUpdatedHandler();
}
