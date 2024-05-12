package apros.codeart.echo.rpc;

import apros.codeart.dto.DTObject;
import apros.codeart.echo.EchoConfig;

public final class EchoRPC {
	private EchoRPC() {
	}

	private static class EchoRPCHolder {

		private static final RPCConfig Config;

		private static final DTObject Section;

		static {

			Config = new RPCConfig();

			DTObject rpc = EchoConfig.rpcSection();
			Config.loadFrom(rpc);

			Section = rpc;
		}
	}

	public static DTObject section() {
		return EchoRPCHolder.Section;
	}

	public static int clientTimeout() {
		return EchoRPCHolder.Config.clientTimeout();
	}

	private static class RPCConfig {
		public RPCConfig() {
			_clientTimeout = 20;
		}

		private int _clientTimeout;

		/**
		 * 
		 * 客户端请求超时的时间
		 * 
		 * @return
		 */
		public int clientTimeout() {
			return _clientTimeout;
		}

		void loadFrom(DTObject root) {
			if (root == null)
				return;
			loadClient(root);
			loadServer(root);
		}

		private void loadClient(DTObject root) {
			_clientTimeout = root.getInt("client.timeout", 20); // 默认20秒超时
		}

		private void loadServer(DTObject root) {

		}
	}

}
