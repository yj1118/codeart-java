package apros.codeart.ddd.cqrs.slave;

import apros.codeart.ddd.cqrs.ActionName;
import apros.codeart.ddd.cqrs.CQRSConfig;
import apros.codeart.ddd.metadata.SchemeCode;
import apros.codeart.ddd.metadata.internal.MetadataLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.mq.rpc.client.RPCClient;

public final class Brancher {
	private Brancher() {
	}

	public static void initialize() {
		loadRemoteObjectMeta();
	}

	private static void loadRemoteObjectMeta() {
		var slaves = CQRSConfig.slaves();
		for (var slave : slaves) {
			var scheme = getRemoteObjectMeta(slave.name());
			var dynamicType = SchemeCode.parse(scheme);
			MetadataLoader.register(dynamicType);
		}
	}

	private static DTObject getRemoteObjectMeta(String name) {

		var methodName = ActionName.getObjectMeta(name);
		return RPCClient.invoke(methodName, (arg) -> {
			arg.setString("name", name);
		}).info();
	}

}