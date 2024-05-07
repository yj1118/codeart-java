package apros.codeart.rabbitmq;

import static apros.codeart.runtime.Util.propagate;

import java.util.function.Function;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import apros.codeart.pooling.IPoolItem;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.util.LazyIndexer;

final class ConnectionManager implements AutoCloseable {

	private Policy _policy;

	private final Connection _conn;

	private final Pool<Channel> _channelPool;

	private ConnectionManager(Policy policy) {
		_policy = policy;
		_conn = createConnection();
		_channelPool = new Pool<Channel>(Channel.class, new PoolConfig(10, 200), (isTempItem) -> {
			return createChannel();
		}, (channel) -> {
			// 归还到池后什么也不用做
		}, (channel) -> {
			try {
				channel.close();
			} catch (Exception e) {
				throw propagate(e);
			}
		});
	}

	private Connection createConnection() {
		try {
			var factory = new ConnectionFactory();
			_policy.init(factory);
			return factory.newConnection();
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private Channel createChannel() {
		try {
			var channel = _conn.createChannel();
			if (_policy.publisherConfirms())
				channel.confirmSelect();

			if (_policy.prefetchCount() == 1) {
				// 作此设计就是为了实现这 RabbitMQ 服务器将会确保在消费者没有确认当前消息之前，不会向该消费者发送多于一个未被确认的消息。
				// 这用于消费者，这有助于实现更加公平的负载均衡，因为它确保一个消费者在处理完当前消息并发送确认之前，不会接收到更多消息。
				channel.basicQos(1);
			} else
				channel.basicQos(0, _policy.prefetchCount(), false);
			return channel;
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private IPoolItem borrow() {
		return _channelPool.borrow();
	}

	@Override
	public void close() {
		try {
			_channelPool.dispose();
			_conn.close();
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private static final Function<Policy, ConnectionManager> _getManager = LazyIndexer.init((policy) -> {
		return new ConnectionManager(policy);
	});

	public static IPoolItem borrow(Policy policy) {
		var manager = _getManager.apply(policy);
		return manager.borrow();
	}

}
