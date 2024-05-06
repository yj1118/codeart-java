package apros.codeart.ddd.cqrs.slave;

import static apros.codeart.runtime.Util.propagate;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.cqrs.ActionName;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.ConstructorRepositoryImpl;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccess;

class RemoteObjectAdded {

	public static void subscribe(String typeName) {
		var messageName = ActionName.objectAdded(typeName);
		DomainMessage.subscribe(messageName, handler);
	}

	public static void cancel(String typeName) {
		var messageName = ActionName.objectAdded(typeName);
		DomainMessage.cancel(messageName);
	}

	@SafeAccess
	private static class RemoteObjectAddedHandler extends RemoteObjectHandler {

		@Override
		protected void handle(DTObject content) {

			var typeName = content.getString("typeName");
			var data = content.getObject("data");
			var domainType = ObjectMetaLoader.get(typeName).objectType();

			var obj = constructObject(domainType);
			obj.load(data);

			DataContext.using(() -> {
				DataPortal.insert(obj);
			});

		}
	}

	private static DomainObject constructObject(Class<?> objectType) {

		try {
			var constructorTip = ConstructorRepositoryImpl.getTip(objectType, true);
			var constructor = constructorTip.constructor();
			// 远程对象在本地的映射，仓储构造函数一定是无参的
			return (DomainObject) constructor.newInstance(ListUtil.emptyObjects());
		} catch (Exception ex) {
			throw propagate(ex);
		}
	}

	private static final RemoteObjectAddedHandler handler = new RemoteObjectAddedHandler();

}
