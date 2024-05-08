package apros.codeart.rabbitmq.event;

import apros.codeart.mq.event.IPublisher;
import apros.codeart.mq.event.IPublisherFactory;
import apros.codeart.util.SafeAccess;

/**
 * 为事件提供广播服务的广播器
 */
@SafeAccess
public class EventPublisherFactory implements IPublisherFactory {

	public IPublisher create() {
		return EventPublisher.Instance;
	}

	public static final EventPublisherFactory Instance = new EventPublisherFactory();

}