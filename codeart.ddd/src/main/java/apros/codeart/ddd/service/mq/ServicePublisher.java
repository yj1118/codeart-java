package apros.codeart.ddd.service.mq;

import apros.codeart.ddd.service.IServicePublisher;
import apros.codeart.ddd.service.ServiceProviderFactory;
import apros.codeart.echo.rpc.RPCServer;

public final class ServicePublisher implements IServicePublisher {

	private ServicePublisher() {
	}

	@Override
	public void release() {
		var services = ServiceProviderFactory.getAll();
		for (var service : services) {
			RPCServer.register(service.name(), ServiceHandler.Instance);
		}
	}

	public static final ServicePublisher Instance = new ServicePublisher();

}
