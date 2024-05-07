package apros.codeart.rabbitmq;

import static apros.codeart.runtime.Util.propagate;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.util.Strings;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import apros.codeart.i18n.Language;
import apros.codeart.mq.TransferData;
import apros.codeart.pooling.IPoolItem;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.util.LazyIndexer;

class RabbitBus implements AutoCloseable {

	private Policy _policy;

	public Policy policy() {
		return _policy;
	}

	private IPoolItem _channelItem;
	private Channel _channel;

	public Channel channel() {
		return _channel;
	}

	public RabbitBus(Policy policy) {
		_policy = policy;
		_channelItem = ConnectionManager.borrow(_policy);
		_channel = _channelItem.getItem();
	}

	/**
	 * 
	 * 声明交换机
	 * 
	 * @param exchange
	 * @param type
	 * @throws IOException
	 */
	public void exchangeDeclare(String exchange, String type) throws IOException {
		if (this.policy().persistentMessages()) {
			this.channel().exchangeDeclare(exchange, type, true, false, null);
		} else {
			this.channel().exchangeDeclare(exchange, type, false, true, null);
		}
	}

	/**
	 * 
	 * 声明一个队列 {@code queue} 并将其用 {@code routingKey} 绑定到指定的交换机 {@code exchange}
	 * 
	 * @param queue
	 * @param exchange
	 * @param routingKey
	 * @throws Exception
	 */
	public void queueDeclare(String queue, String exchange, String routingKey) throws Exception {
		queueDeclare(queue);
		this.channel().queueBind(queue, exchange, routingKey);
	}

	public void queueDeclare(String queue) throws Exception {
		if (this.policy().persistentMessages()) {
			this.channel().queueDeclare(queue, true, false, false, null);
		} else {
			this.channel().queueDeclare(queue, false, false, true, null); // 最后一个true表示不持久化的消息，服务器端分发后就删除，适用于rpc模式
		}
	}

	/**
	 * 声明临时队列
	 * 
	 * @return 返回队列名称
	 * @throws Exception
	 */
	public String tempQueueDeclare() throws Exception {
		// 临时队列是由rabbit分配名称、只有自己可以看见、用后就删除的队列
		return this.channel().queueDeclare(Strings.EMPTY, false, true, true, null).getQueue();
	}

	/// <summary>
	/// 删除队列
	/// </summary>
	/// <param name="queue"></param>
	public void queueDelete(String queue) throws Exception {
		if (channelIsClosed()) // 如果连接已关闭，证明该队列已经被删除，不必重复删除
			return;
		this.channel().queueDelete(queue);
	}

	private boolean channelIsClosed() {
		return this.channel() != null && !this.channel().isOpen();
	}

//	#region 发布消息

	/// <summary>
	/// 将dto消息发布到指定的交换机
	/// </summary>
	/// <param name="message"></param>
	public void publish(String exchange, String routingKey, TransferData data,
			Consumer<BasicProperties.Builder> setProperties) throws Exception {
		var body = TransferData.serialize(data);
		var propsBuilder = new BasicProperties.Builder().contentEncoding("utf-8").contentType("text/plain");

		if (setProperties != null) {
			setProperties.accept(propsBuilder);
		}

		var channel = this.channel();

		if (this.policy().persistentMessages()) {
			propsBuilder.deliveryMode(2); // 设置为持久化消息
			var properties = propsBuilder.build();

			if (this.policy().publisherConfirms()) {
				confirmPublish(exchange, routingKey, properties, body);
			} else
				channel.basicPublish(exchange, routingKey, properties, body);
		} else {
			propsBuilder.deliveryMode(1); // 设置为非持久化消息
			var properties = propsBuilder.build();
			channel.basicPublish(exchange, routingKey, properties, body);
		}
	}

	private void confirmPublish(String exchange, String routingKey, BasicProperties properties, byte[] body) {
		try {
			this.channel().basicPublish(exchange, routingKey, properties, body);
			this.channel().waitForConfirmsOrDie(20_000); // 等待20秒以确认消息被送达
		} catch (Exception e) {
			throw new RabbitMQException(Language.strings("codeart", "PublishMessageFailed", routingKey));
		}
	}

//	#endregion

	private IMessageHandler _messageHandler = null;

	public void consume(String queue, IMessageHandler handler) throws Exception {
		_messageHandler = handler;
		accept(queue, false);
	}

	private void accept(String queue, boolean autoAck) throws Exception {

		var consumer = new DefaultConsumer(this.channel()) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
				messageReceived(envelope, properties, body);
			}
		};

		this.channel().basicConsume(queue, autoAck, consumer);
	}

	private void messageReceived(Envelope envelope, BasicProperties properties, byte[] body) {
		// 此处必须异步，否则会阻塞RPCServer接收处理消息，导致一个请求处理完后才处理下一个请求，吞吐量大幅度降低Task.Run(() =>
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			executor.submit(() -> {
				// DTObject content = DTObject.Create(e.Body);
				var content = TransferData.deserialize(body);

				var message = new Message(content, properties, () -> {
					ack(envelope);
				}, (requeue) -> {
					reject(envelope, requeue);
				});
				_messageHandler.handle(message);
			});
		}
	}

	private void ack(Envelope envelope) {
		try {
			this.channel().basicAck(envelope.getDeliveryTag(), false);
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	private void reject(Envelope envelope, boolean requeue) {
		try {
			this.channel().basicReject(envelope.getDeliveryTag(), requeue);
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	@Override
	public void close() {
		if (_channelItem != null) {
			_channelItem.back();
			_channelItem = null;
			_channel = null;
		}
	}

	public static IPoolItem borrow(Policy policy) {
		var pool = _getPool.apply(policy);
		return pool.borrow();
	}

	private static Function<Policy, Pool<RabbitBus>> _getPool = LazyIndexer.init((policy) -> {
		return new Pool<RabbitBus>(RabbitBus.class, new PoolConfig(10, 200), (isTempItem) -> {
			return new RabbitBus(policy);
		}, null, (bus) -> {
			bus.close();
		});
	});
}
