package apros.codeart.rabbitmq.event;

import apros.codeart.echo.IEchoProvider;
import apros.codeart.echo.event.EventPortal;
import apros.codeart.util.SafeAccess;

@SafeAccess
public final class RabbitMQEventProvider implements IEchoProvider {

	@Override
	public String name() {
		return "rabbitMQ-event";
	}

	@Override
	public void setup() {
		EventPortal.register(EventPublisherFactory.Instance);
		EventPortal.register(EventSubscriberFactory.Instance);
	}
}
