package apros.codeart.rabbitmq;

import apros.codeart.mq.IMQProvider;
import apros.codeart.mq.event.EventPortal;
import apros.codeart.mq.rpc.client.RPCClient;
import apros.codeart.mq.rpc.server.RPCServer;
import apros.codeart.rabbitmq.event.EventPublisherFactory;
import apros.codeart.rabbitmq.event.EventSubscriberFactory;
import apros.codeart.rabbitmq.rpc.RPCClientFactory;
import apros.codeart.rabbitmq.rpc.RPCServerFactory;

public final class RabbitMQProvider implements IMQProvider {

	@Override
	public String name() {
		return "rabbitMQ";
	}

	@Override
	public void setup() {
		EventPortal.register(EventPublisherFactory.Instance);
		EventPortal.register(EventSubscriberFactory.Instance);

		RPCClient.register(RPCClientFactory.Instance);
		RPCServer.Register(RPCServerFactory.Instance);
	}

}
