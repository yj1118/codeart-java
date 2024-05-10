package apros.codeart.echo;

import apros.codeart.AppConfig;
import apros.codeart.dto.DTObject;

public final class EchoConfig {
	private EchoConfig() {
	}

	private static class Holder {

		private static final DTObject EventSection;

		private static final DTObject RPCSection;

		static {

			var event = AppConfig.section("event");
			var rpc = AppConfig.section("rpc");

			EventSection = event == null ? DTObject.Empty : event;
			RPCSection = rpc == null ? DTObject.Empty : rpc;
		}
	}

	public static DTObject eventSection() {
		return Holder.EventSection;
	}

	public static DTObject rpcSection() {
		return Holder.RPCSection;
	}

}
