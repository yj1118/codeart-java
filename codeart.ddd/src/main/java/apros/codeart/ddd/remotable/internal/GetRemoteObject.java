package apros.codeart.ddd.remotable.internal;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.rpc.server.IRPCHandler;
import apros.codeart.util.PrimitiveUtil;

final class GetRemoteObject implements IRPCHandler {
	private GetRemoteObject() {
	}

	public TransferData process(String method, DTObject arg) {
		// 先初始化会话身份
		initIdentity(arg);

		var tip = getTip(arg);
		var obj = findObject(tip, arg);
		var schemaCode = getSchemaCode(tip, arg);
		var info = DTObject.readonly(schemaCode, obj);
		return new TransferData(AppSession.language(), info);
	}

	private void initIdentity(DTObject arg) {
		var identity = arg.getObject("identity");
		AppSession.setIdentity(identity);
	}

	private RemotableImpl getTip(DTObject arg) {
		var typeName = arg.getString("typeName");
		return RemotableImpl.getTip(typeName);
	}

	private Object findObject(RemotableImpl tip, DTObject arg) {
		var idProperty = DomainProperty.getProperty(tip.objectType(), EntityObject.IdPropertyName);
		var id = PrimitiveUtil.convert(arg.getValue("id"), idProperty.monotype());

		var repository = Repository.createByObjectType(tip.objectType());
		return repository.findRoot(id, QueryLevel.None);
	}

	private String getSchemaCode(RemotableImpl tip, DTObject arg) {
		return arg.getString("schemaCode");
	}

	public static final GetRemoteObject instance = new GetRemoteObject();
}
