package apros.codeart.rabbitmq.event;

import static apros.codeart.runtime.Util.propagate;

import apros.codeart.context.AppSession;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.TransferData;
import apros.codeart.echo.event.IPublisher;
import apros.codeart.rabbitmq.RabbitBus;
import apros.codeart.util.SafeAccess;

@SafeAccess
class EventPublisher implements IPublisher {

	private EventPublisher() {
	}

	/**
	 * 发布事件，不需要等返回值，所以用单例发送，方法体里会用不同的bug发送事件
	 */
	@Override
	public void publish(String eventName, DTObject arg) {
		try (var temp = RabbitBus.borrow(EventConfig.PublisherPolicy)) {
			RabbitBus bus = temp.getItem();
			try {
				bus.exchangeDeclare(EventConfig.Exchange, "topic");
				var routingKey = eventName;
				bus.publish(EventConfig.Exchange, routingKey, new TransferData(AppSession.language(), arg), null);
			} catch (Exception e) {
				throw propagate(e);
			}
		}
	}

	public static final EventPublisher Instance = new EventPublisher();

}
