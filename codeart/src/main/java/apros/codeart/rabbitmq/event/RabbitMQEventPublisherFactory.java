package apros.codeart.rabbitmq.event;

import apros.codeart.echo.event.IPublisher;
import apros.codeart.echo.event.IPublisherFactory;
import apros.codeart.util.SafeAccess;

/**
 * 为事件提供广播服务的广播器
 */
@SafeAccess
public class RabbitMQEventPublisherFactory implements IPublisherFactory {

	public IPublisher create() {
		return EventPublisher.Instance;
	}

	public static final RabbitMQEventPublisherFactory Instance = new RabbitMQEventPublisherFactory();

}