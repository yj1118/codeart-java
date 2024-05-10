package apros.codeart.rabbitmq.event;

import apros.codeart.IModuleProvider;
import apros.codeart.echo.event.EventPortal;
import apros.codeart.util.SafeAccess;

@SafeAccess
public final class RabbitMQEventProvider implements IModuleProvider {

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
