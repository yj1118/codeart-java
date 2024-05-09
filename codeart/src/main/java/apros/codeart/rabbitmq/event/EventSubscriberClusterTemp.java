package apros.codeart.rabbitmq.event;

import java.time.Duration;

import apros.codeart.mq.event.IEventHandler;
import apros.codeart.mq.event.ISubscriber;
import apros.codeart.rabbitmq.IConsumerCluster;

class EventSubscriberClusterTemp implements IConsumerCluster, ISubscriber {

	private EventSubscriber _subscriber;

	public EventSubscriberClusterTemp(String eventName, String group) {
		_subscriber = new EventSubscriber(this, EventConfig.getQueue(eventName, group));
	}

	@Override
	public void accept() {
		_subscriber.accept();

	}

	@Override
	public void stop() {
		_subscriber.stop();
	}

	@Override
	public void addHandler(IEventHandler handler) {
		_subscriber.addHandler(handler);
	}

	@Override
	public void remove() {
		_subscriber.remove();
	}

	@Override
	public void tryScale() {
		// 什么也不用做
	}

	@Override
	public void messagesProcessed(Duration elapsed) {
		// 什么也不用做
	}
}
