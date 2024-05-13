package apros.codeart.gate.service.mq;

import apros.codeart.dto.DTObject;
import apros.codeart.echo.rpc.RPCClient;
import apros.codeart.gate.service.IServiceProxy;

public class MQServiceProxy implements IServiceProxy {

	private MQServiceProxy() {
	}

	public DTObject invoke(String serviceName, DTObject arg) {
		return RPCClient.invoke(serviceName, arg);
	}

	public static final MQServiceProxy Instance = new MQServiceProxy();
}
