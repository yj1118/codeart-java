package apros.codeart.rabbitmq.event;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import apros.codeart.echo.event.IEventHandler;
import apros.codeart.echo.event.ISubscriber;
import apros.codeart.rabbitmq.IConsumerCluster;
import apros.codeart.util.SafeAccessImpl;

class EventSubscriberClusterTemp implements IConsumerCluster, ISubscriber, IEventSubscriberCluster {

    private final EventSubscriber _subscriber;

    public EventSubscriberClusterTemp(String eventName, String group) {
        _subscriber = new EventSubscriber(this, eventName, group);
    }

    @Override
    public void accept() {
        _subscriber.accept();

    }

    @Override
    public void stop() {
        _subscriber.stop();
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
