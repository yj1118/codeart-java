package apros.codeart.mq.rpc;

import apros.codeart.AppConfig;
import apros.codeart.InterfaceImplementer;
import apros.codeart.dto.DTObject;

public final class MQRPC {
	private MQRPC() {
	}

	private static class MQRPCHolder {

		private static final RPCConfig Config;

		private static final DTObject Section;

		static {

			Config = new RPCConfig();

			var mq = AppConfig.section("mq");
			DTObject rpc = null;
			if (mq != null) {
				rpc = mq.getObject("rpc", null);
				Config.loadFrom(rpc);
			}

			Section = rpc == null ? DTObject.Empty : rpc;
		}
	}

	public static DTObject section() {
		return MQRPCHolder.Section;
	}

	public static InterfaceImplementer getClientFactoryImplementer() {
		return MQRPCHolder.Config.clientFactoryImplementer();
	}

	public static InterfaceImplementer getServerFactoryImplementer() {
		return MQRPCHolder.Config.serverFactoryImplementer();
	}

	public static int clientTimeout() {
		return MQRPCHolder.Config.clientTimeout();
	}

	private static class RPCConfig {
		public RPCConfig() {
			_clientTimeout = 20;
		}

		private InterfaceImplementer _clientFactoryImplementer;

		public InterfaceImplementer clientFactoryImplementer() {
			return _clientFactoryImplementer;
		}

		private InterfaceImplementer _serverFactoryImplementer;

		public InterfaceImplementer serverFactoryImplementer() {
			return _serverFactoryImplementer;
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
			var factory = root.getObject("client.factory", null);

			if (factory != null)
				_serverFactoryImplementer = InterfaceImplementer.create(factory);
		}
	}

}
