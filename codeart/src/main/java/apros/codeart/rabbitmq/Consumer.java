package apros.codeart.rabbitmq;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Consumer implements IConsumer, IMessageHandler {

	private IConsumerCluster _cluster;

	private AtomicBoolean _closed;

	public Consumer(IConsumerCluster cluster) {
		_cluster = cluster;
		_closed = new AtomicBoolean(false);
	}

	@Override
	public final void handle(RabbitBus sender, Message message) {
		var elapsed = message.success();

		if (_closed.getAcquire()) {
			// 先关闭
			this.dispose();
		}
		_cluster.messagesProcessed(elapsed);
	}

	public abstract void dispose();

	protected abstract Duration processMessage(RabbitBus sender, Message message);

	/**
	 * 直到下次处理完一个请求后才会真正关闭
	 */
	@Override
	public void close() {
		_closed.setRelease(true);
	}

}
