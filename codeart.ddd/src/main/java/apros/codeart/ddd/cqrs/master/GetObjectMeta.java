package apros.codeart.ddd.cqrs.master;

import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.rpc.IRPCHandler;

final class GetObjectMeta implements IRPCHandler {
	private GetObjectMeta() {
	}

	public DTObject process(String method, DTObject arg) {

		var name = arg.getString("name");

		var master = Forker.findMaster(name, true);
		var meta = ObjectMetaLoader.get(name);

		var scheme = meta.toDTO(master.members());

		return scheme;
	}

	public static final GetObjectMeta Instance = new GetObjectMeta();
}