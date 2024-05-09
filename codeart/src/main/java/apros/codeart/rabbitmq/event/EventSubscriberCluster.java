package apros.codeart.rabbitmq.event;

import apros.codeart.mq.event.IEventHandler;
import apros.codeart.mq.event.ISubscriber;
import apros.codeart.rabbitmq.ConsumerCluster;

class EventSubscriberCluster extends ConsumerCluster<EventSubscriber> implements ISubscriber {

	public EventSubscriberCluster(String eventName, String group) {
		super(EventConfig.SubscriberPolicy, EventConfig.getServerConfig(eventName),
				EventConfig.getQueue(eventName, group), (cluster) -> {
					var rc = (EventSubscriberCluster) cluster;
					return new EventSubscriber(rc, rc.queue());
				});
	}

	@Override
	public void accept() {
		var items = this.consumers();
		for (var item : items)
			item.accept();

	}

	@Override
	public void stop() {
		var items = this.consumers();
		for (var item : items)
			item.stop();
	}

	@Override
	public void addHandler(IEventHandler handler) {
		var items = this.consumers();
		for (var item : items)
			item.addHandler(handler);
	}

	@Override
	public void remove() {
		var items = this.consumers();
		for (var item : items)
			item.remove();
	}
}
