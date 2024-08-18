package apros.codeart.rabbitmq.event;

import apros.codeart.echo.event.IEventHandler;
import apros.codeart.rabbitmq.IConsumerCluster;

import java.util.List;


public interface IEventSubscriberCluster extends IConsumerCluster {
    List<IEventHandler> handlers();

    void addHandler(IEventHandler handler);
}
