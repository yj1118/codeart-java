package apros.codeart.rabbitmq.event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import apros.codeart.mq.event.ISubscriber;
import apros.codeart.mq.event.ISubscriberFactory;
import apros.codeart.mq.event.MQEvent;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class EventSubscriberFactory implements ISubscriberFactory {

	public ISubscriber create(String eventName, boolean cluster) {
		var subscriber = _map.get(eventName);
		if (subscriber == null) {
			synchronized (_map) {
				subscriber = _map.get(eventName);
				if (subscriber == null) {
					var group = MQEvent.getSubscriberGroup();
					subscriber = cluster ? new EventSubscriberCluster(eventName, group)
							: new EventSubscriberClusterTemp(eventName, group);

					_map.put(eventName, subscriber);
					_items.add(subscriber);
				}
			}
		}
		return subscriber;
	}

	@Override
	public ISubscriber get(String eventName) {
		return _map.get(eventName);
	}

	public Iterable<ISubscriber> getAll() {
		return _items;
	}

	private static ConcurrentLinkedQueue<ISubscriber> _items = new ConcurrentLinkedQueue<ISubscriber>();

	private static ConcurrentHashMap<String, ISubscriber> _map = new ConcurrentHashMap<String, ISubscriber>();

	public ISubscriber remove(String eventName) {
		var subscriber = _map.remove(eventName);
		if (subscriber != null) {
			_items.remove(subscriber);
		}
		return subscriber;
	}

	public static final EventSubscriberFactory Instance = new EventSubscriberFactory();

}
