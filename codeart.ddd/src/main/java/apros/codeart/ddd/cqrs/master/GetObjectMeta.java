package apros.codeart.ddd.cqrs.master;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.metadata.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.rpc.server.IRPCHandler;

final class GetObjectMeta implements IRPCHandler {
	private GetObjectMeta() {
	}

	public TransferData process(String method, DTObject arg) {

		var name = arg.getString("name");

		var master = Forker.findMaster(name, true);
		var meta = ObjectMetaLoader.get(name);

		var scheme = meta.toDTO(master.members());

		return new TransferData(AppSession.language(), scheme);
	}

	public static final GetObjectMeta Instance = new GetObjectMeta();
}