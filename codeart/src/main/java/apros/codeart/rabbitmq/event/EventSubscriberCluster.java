package apros.codeart.rabbitmq.event;

import apros.codeart.echo.event.IEventHandler;
import apros.codeart.echo.event.ISubscriber;
import apros.codeart.rabbitmq.ConsumerCluster;
import apros.codeart.util.SafeAccessImpl;

import java.util.ArrayList;
import java.util.List;

class EventSubscriberCluster extends ConsumerCluster<EventSubscriber> implements ISubscriber, IEventSubscriberCluster {


    public EventSubscriberCluster(String eventName, String group) {
        super(EventConfig.SubscriberPolicy, EventConfig.getServerConfig(eventName),
                EventConfig.getQueue(eventName, group), (cluster) -> {
                    return new EventSubscriber((IEventSubscriberCluster) cluster, eventName, group);
                });
    }

    @Override
    public void accept() {
        // 一开始没有任何消费者，所以这里只用open，确保有一个消费者，后面会根据吞吐量自动伸缩
        this.open();
    }

    @Override
    public void stop() {
        var items = this.consumers();
        for (var item : items)
            item.stop();
    }

    private final ArrayList<IEventHandler> _handlers = new ArrayList<IEventHandler>();

    public List<IEventHandler> handlers() {
        return _handlers;
    }

    @Override
    public void addHandler(IEventHandler handler) {
        SafeAccessImpl.checkUp(handler);

        synchronized (_handlers) {
            if (!_handlers.contains(handler)) {
                _handlers.add(handler);
            }
        }
    }

    @Override
    public void remove() {
        var items = this.consumers();
        for (var item : items)
            item.remove();
    }
}
