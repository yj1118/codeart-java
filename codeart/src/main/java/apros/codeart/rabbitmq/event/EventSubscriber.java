package apros.codeart.rabbitmq.event;

import java.time.Duration;
import java.util.ArrayList;

import apros.codeart.context.AppSession;
import apros.codeart.log.Logger;
import apros.codeart.mq.event.IEventHandler;
import apros.codeart.mq.event.ISubscriber;
import apros.codeart.pooling.IPoolItem;
import apros.codeart.rabbitmq.Message;
import apros.codeart.rabbitmq.RabbitBus;
import apros.codeart.rabbitmq.internal.Consumer;
import apros.codeart.rabbitmq.internal.IConsumerCluster;
import apros.codeart.util.SafeAccessImpl;
import apros.codeart.util.thread.Parallel;

/**
 * 消息队列可以把1个消息同时发布给多个队列，只要这些队列的routingKey和消息发布的routingKey相同或相匹配即可。
 * 
 * 每个队列里的消费者A/B/C..,会轮流得到消息并且消费。
 * 
 * 不同队列里的消费者会同时获得消息来处理，而不是轮询。
 * 
 * 以上规则就建立了分布式的基础：可以共同消费保证业务抵达也可以轮询消费实现均衡负载。
 * 
 */
class EventSubscriber extends Consumer implements AutoCloseable, ISubscriber {

	private String _eventName;
	private String _group;
	private String _queue;

	private IPoolItem _busItem;

	public String eventName() {
		return _eventName;
	}

	public String group() {
		return _group;
	}

	public EventSubscriber(IConsumerCluster cluster, String queue) {
		super(cluster);
		_queue = queue;
		_busItem = RabbitBus.borrow(EventConfig.SubscriberPolicy);
	}

	public void accept() {

		RabbitBus bus = _busItem.getItem();
		bus.exchangeDeclare(EventConfig.Exchange, "topic");

		var routingKey = _eventName;
		bus.queueDeclare(_queue, EventConfig.Exchange, routingKey);
		bus.consume(_queue, this);
	}

	@Override
	public boolean disposed() {
		return _busItem == null;
	}

	public void dispose() {
		if (_busItem != null) {
			_busItem.close();
			_busItem = null;
		}
	}

	/**
	 * 这个是真正删除的操作，主要用于一些临时队列， 由于临时队列是用完后删除，由外界保证正确调用的时机即可， remove内部不需要考虑bus是否正在工作
	 */
	@Override
	public void remove() {
		RabbitBus bus = _busItem.getItem();
		bus.queueDelete(_queue); // 这个是真正删除的操作，主要用于一些临时队列
		_busItem.close(); // 删除队列后，清理资源
	}

	private ArrayList<IEventHandler> _handlers = new ArrayList<IEventHandler>();

	public void addHandler(IEventHandler handler) {
		SafeAccessImpl.checkUp(handler);

		synchronized (_handlers) {
			if (!_handlers.contains(handler)) {
				_handlers.add(handler);

			}
		}
	}

	@Override
	public void open() {
		this.accept();
	}

	@Override
	public void stop() {
		this.close();
	}

	/**
	 * 请自行保证事件的幂等性
	 */
	@Override
	protected Duration processMessage(RabbitBus sender, Message message) {
		if (_handlers.size() == 0)
			return Duration.ZERO;

		// 以下代码段反映的是这样一个逻辑：
		// 如果是框架产生的异常，那么我们会告诉RabbitMQ服务器重发消息给下一个订阅者
		// 如果是事件内部报错，程序员应该自己捕获错误，然后去处理，这时候异常不会被抛出，那么消息就算被处理完了
		// 如果事件内部被程序员抛出了异常，那么会被写入日志，并且提示RabbitMQ服务器重发消息给下一个订阅者，重新处理
		// 在这种情况下，由于事件会挂载多个，其中一个出错，前面执行的事件也会被重复执行，所以我们要保证事件的幂等性
		Duration elapsed = null;
		var arg = message.content();
		var language = message.language();
		try {

			Parallel.forEach(_handlers, (handler) -> {
				AppSession.using(() -> {
					AppSession.setLanguage(language);
					handler.handle(_eventName, arg);
				});
			});
			elapsed = message.success();
		} catch (Exception ex) {
			Logger.fatal(ex);
			elapsed = message.failed(true); // true:提示RabbitMQ服务器重发消息给下一个订阅者，false:提示RabbitMQ服务器把消息从队列中移除
		}
		return elapsed;
	}
}
