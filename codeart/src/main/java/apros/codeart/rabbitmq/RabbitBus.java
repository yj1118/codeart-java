package apros.codeart.rabbitmq;

import static apros.codeart.runtime.Util.propagate;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.util.Strings;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Envelope;

import apros.codeart.echo.TransferData;
import apros.codeart.i18n.Language;
import apros.codeart.pooling.IPoolItem;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.util.LazyIndexer;

public class RabbitBus implements AutoCloseable {

	private Policy _policy;

	public Policy policy() {
		return _policy;
	}

	private IPoolItem _channelItem;
	private Channel _channel;

	Channel channel() {
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
	public void exchangeDeclare(String exchange, String type) {

		try {
			if (this.policy().persistentMessages()) {
				this.channel().exchangeDeclare(exchange, type, true, false, null);
			} else {
				this.channel().exchangeDeclare(exchange, type, false, true, null);
			}
		} catch (Exception ex) {
			throw propagate(ex);
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
	public void queueDeclare(String queue, String exchange, String routingKey) {
		queueDeclare(queue);
		try {
			this.channel().queueBind(queue, exchange, routingKey);
		} catch (Exception ex) {
			throw propagate(ex);
		}
	}

	public void queueDeclare(String queue) {
		try {
			if (this.policy().persistentMessages()) {
				this.channel().queueDeclare(queue, true, false, false, null);
			} else {
				this.channel().queueDeclare(queue, false, false, true, null); // 最后一个true表示不持久化的消息，服务器端分发后就删除，适用于rpc模式
			}
		} catch (Exception ex) {
			throw propagate(ex);
		}
	}

	/**
	 * 声明临时队列
	 * 
	 * @return 返回队列名称
	 * @throws Exception
	 */
	public String tempQueueDeclare() {
		try {
			// 临时队列是由rabbit分配名称、只有自己可以看见、用后就删除的队列
			return this.channel().queueDeclare(Strings.EMPTY, false, true, true, null).getQueue();
		} catch (Exception ex) {
			throw propagate(ex);
		}

	}

	/**
	 * 
	 * 删除队列
	 * 
	 * @param queue
	 * @throws Exception
	 */
	public void queueDelete(String queue) {
		try {
			this.channel().queueDelete(queue);
		} catch (Exception ex) {
			throw propagate(ex);
		}
	}

//	private boolean channelIsClosed() {
//		return this.channel() != null && !this.channel().isOpen();
//	}

//	#region 发布消息

	/**
	 * 
	 * 将dto消息发布到指定的交换机
	 * 
	 * @param exchange
	 * @param routingKey
	 * @param data
	 * @param setProperties
	 */
	public void publish(String exchange, String routingKey, TransferData data,
			Consumer<BasicProperties.Builder> setProperties) {

		try {
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
		} catch (Exception e) {
			throw new RabbitMQException(Language.strings("apros.codeart", "PublishMessageFailed", routingKey));
		}
	}

	private void confirmPublish(String exchange, String routingKey, BasicProperties properties, byte[] body) {
		try {
			this.channel().basicPublish(exchange, routingKey, properties, body);
			this.channel().waitForConfirmsOrDie(20_000); // 等待20秒以确认消息被送达
		} catch (Exception e) {
			throw new RabbitMQException(Language.strings("apros.codeart", "PublishMessageFailed", routingKey));
		}
	}

//	#endregion

	private IMessageHandler _messageHandler = null;

	/**
	 * 消费者标识
	 */
	private String _consumerTag;

	public void consume(String queue, IMessageHandler handler) {
		if (_consumerTag != null)
			throw new RabbitMQException(Language.strings("apros.codeart", "MoreConsume", queue));

		_messageHandler = handler;
		// 不论什么应用，都需要手工应答，主要是为了避免不做限制，导致服务器资源耗尽
		accept(queue, false);
	}

	private void accept(String queue, boolean autoAck) {
		try {
			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				messageReceived(consumerTag, delivery);
			};

			_consumerTag = this.channel().basicConsume(queue, autoAck, deliverCallback, consumerTag -> {
			});
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	private void messageReceived(String consumerTag, Delivery delivery) {

		long startTime = System.currentTimeMillis();

		var content = TransferData.deserialize(delivery.getBody());

		var message = new Message(content, delivery.getProperties(), () -> {
			ack(delivery.getEnvelope());
			long endTime = System.currentTimeMillis();
			return Duration.ofMillis(endTime - startTime);
		}, (requeue) -> {
			reject(delivery.getEnvelope(), requeue);
			long endTime = System.currentTimeMillis();
			return Duration.ofMillis(endTime - startTime);
		});
		_messageHandler.handle(this, message);

//		// 此处必须异步，否则会阻塞RPCServer接收处理消息，导致一个请求处理完后才处理下一个请求，吞吐量大幅度降低
//		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
//			executor.submit(() -> {
//				
//			});
//		}
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

	public static int getMessageCount(Policy policy, String queue) {
		try (var temp = ConnectionManager.borrow(policy)) {
			try {
				Channel channel = temp.getItem();
				var dok = channel.queueDeclarePassive(queue);
				return dok.getMessageCount();
			} catch (IOException e) {
				throw propagate(e);
			}
		}
	}

	private void clear() {
		// 取消消费
		this.cancel();
	}

	/**
	 * 取消消费
	 */
	public void cancel() {
		if (_consumerTag == null)
			return;
		try {
			this.channel().basicCancel(_consumerTag);
			_consumerTag = null;
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	@Override
	public void close() {
		// 销毁bus，就要把它用到的通道资源归还
		if (_channelItem != null) {
			this.cancel();
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
		return new Pool<RabbitBus>(RabbitBus.class, new PoolConfig(10, 200, 60), (isTempItem) -> {
			return new RabbitBus(policy);
		}, (bus) -> {
			// 归还bus，需要取消消费
			bus.clear();
		}, (bus) -> {
			bus.close();
		});
	});
}
