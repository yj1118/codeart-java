package apros.codeart.mq.rpc.client;

import apros.codeart.mq.rpc.MQRPC;

public class ClientConfig {

	private int _timeout;

	/**
	 * 
	 * 请求超时时间
	 * 
	 * @return
	 */
	public int timeout() {
		return _timeout;
	}

	private ClientConfig(int timeout) {
		_timeout = timeout;
	}

	public static final ClientConfig Instance = new ClientConfig(MQRPC.clientTimeout());
}
