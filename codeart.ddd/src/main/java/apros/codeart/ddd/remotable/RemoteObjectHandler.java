package apros.codeart.ddd.remotable;

import static apros.codeart.i18n.Language.strings;

import java.util.function.BiConsumer;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.metadata.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.event.EventPriority;
import apros.codeart.mq.event.IEventHandler;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.PrimitiveUtil;

public abstract class RemoteObjectHandler implements IEventHandler {

	public EventPriority getPriority() {
		return EventPriority.High;
	}

	public void handle(String eventName, TransferData data) {
		var arg = data.info();
		AppSession.setIdentity(arg.getObject("identity")); // 先初始化身份
		handle(arg);
	}

	protected abstract void handle(DTObject arg);

//	#region 辅助方法

	protected void useDefine(DTObject arg, BiConsumer<Class<? extends DynamicRoot>, Object> action) {
		var typeName = arg.getString("typeName");
		var meatdata = ObjectMetaLoader.get(typeName);
		var rootType = TypeUtil.as(meatdata.objectType(), DynamicRoot.class);

		if (rootType == null) {
			throw new IllegalArgumentException(strings("codeart.ddd", "NotDynamicRootType", typeName));
		}

		var idProperty = meatdata.findProperty(EntityObject.IdPropertyName);
		var id = PrimitiveUtil.convert(arg.getValue(EntityObject.IdPropertyName), idProperty.monotype());

		action.accept(rootType, id);
	}

//	#endregion

}
