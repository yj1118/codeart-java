package apros.codeart.ddd.remotable;

import static apros.codeart.i18n.Language.strings;

import java.util.function.BiConsumer;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.message.DomainMessageHandler;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.event.EventPriority;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.PrimitiveUtil;

public abstract class RemoteObjectHandler extends DomainMessageHandler {

	@Override
	public EventPriority getPriority() {
		return EventPriority.High;
	}

	@Override
	public void process(String msgName, String msgId, DTObject content) {
		AppSession.setIdentity(content.getObject("identity")); // 先初始化身份
		handle(content);
	}

	protected abstract void handle(DTObject content);

//	#region 辅助方法

	protected void useDefine(DTObject arg, BiConsumer<Class<? extends DynamicRoot>, Object> action) {
		var typeName = arg.getString("typeName");
		var meatdata = ObjectMetaLoader.get(typeName);
		var rootType = TypeUtil.asT(meatdata.objectType(), DynamicRoot.class);

		if (rootType == null) {
			throw new IllegalArgumentException(strings("codeart.ddd", "NotDynamicRootType", typeName));
		}

		var idProperty = meatdata.findProperty(EntityObject.IdPropertyName);
		var id = PrimitiveUtil.convert(arg.getValue(EntityObject.IdPropertyName), idProperty.monotype());

		action.accept(rootType, id);
	}

//	#endregion

}
