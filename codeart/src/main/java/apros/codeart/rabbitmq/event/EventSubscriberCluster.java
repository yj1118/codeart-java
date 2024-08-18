package apros.codeart.rabbitmq.event;

import apros.codeart.echo.event.IEventHandler;
import apros.codeart.echo.event.ISubscriber;
import apros.codeart.rabbitmq.ConsumerCluster;

class EventSubscriberCluster extends ConsumerCluster<EventSubscriber> implements ISubscriber {

    public EventSubscriberCluster(String eventName, String group) {
        super(EventConfig.SubscriberPolicy, EventConfig.getServerConfig(eventName),
                EventConfig.getQueue(eventName, group), (cluster) -> {
                    return new EventSubscriber(cluster, eventName, group);
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
