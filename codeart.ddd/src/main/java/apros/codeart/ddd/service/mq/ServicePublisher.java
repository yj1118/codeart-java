package apros.codeart.ddd.service.mq;

import apros.codeart.ddd.service.IServicePublisher;
import apros.codeart.ddd.service.ServiceProviderFactory;
import apros.codeart.echo.rpc.RPCServer;
import apros.codeart.rabbitmq.rpc.RPCConfig;
import apros.codeart.util.StringUtil;

public final class ServicePublisher implements IServicePublisher {

    private ServicePublisher() {
    }

    @Override
    public void release() {
        var services = ServiceProviderFactory.getAll();
        for (var service : services) {
            var name = StringUtil.isNullOrEmpty(service.name()) ?
                    service.provider().getClass().getSimpleName() :
                    service.name();
            // 对外的服务不提供临时队列即可
            RPCServer.register(name, ServiceHandler.Instance, RPCConfig.ServerTransient);
        }
    }

    public static final ServicePublisher Instance = new ServicePublisher();

}
