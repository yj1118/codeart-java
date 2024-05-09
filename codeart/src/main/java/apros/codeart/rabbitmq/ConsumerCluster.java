package apros.codeart.rabbitmq;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import apros.codeart.util.ListUtil;
import apros.codeart.util.thread.Timer;
import apros.codeart.util.thread.Timer.LoopType;

public abstract class ConsumerCluster<T extends IConsumer> implements IConsumerCluster, AutoCloseable {

	private final Policy _policy;

	private final ConsumerConfig _config;

	private final String _queue;

	public String queue() {
		return _queue;
	}

	private final ConcurrentLinkedQueue<T> _consumers = new ConcurrentLinkedQueue<T>();

	protected Iterable<T> consumers() {
		return _consumers;
	}

	private final Function<ConsumerCluster<T>, T> _createConsumer;

	public ConsumerCluster(Policy policy, ConsumerConfig config, String queue,
			Function<ConsumerCluster<T>, T> createConsumer) {
		_policy = policy;
		_config = config;
		_queue = queue;
		_createConsumer = createConsumer;
	}

	/**
	 * 
	 * 队列中有多少没有处理的消息数量
	 * 
	 * @return
	 */
	public int getMessageCount() {
		return RabbitBus.getMessageCount(_policy, _queue);
	}

	private void increase() {
		var server = _createConsumer.apply(this);
		server.open();
		_consumers.add(server);
	}

	private void reduce() {
		// 减少一个服务
		var item = _consumers.poll();
		if (item != null) {
			item.close();
			// 由于关闭服务是延迟的，需要等待下次处理一个请求后再关闭，所以这里得用_waiteDispose持有它得引用，否则被jvm回收了，引起BUG
			_waiteDispose.add(item);
		}
	}

	public void open() {
		this.increase();
	}

	@Override
	public void close() {
		for (var s : _consumers)
			s.close();
	}

	/**
	 * 最后一次记录的消息积压
	 */
	private int _lastBacklog;

	private AtomicInteger _processed = new AtomicInteger(0);

	/**
	 * 处理消息花费的时间
	 */
	private AtomicLong _elapsedMillis = new AtomicLong(0);

	/**
	 * 指示一条消息被处理完毕了
	 * 
	 * @param elapsed 处理这条消息花费的时间
	 */
	public void messagesProcessed(Duration elapsed) {
		_processed.incrementAndGet();
		_elapsedMillis.addAndGet(elapsed.toMillis());
	}

	private void resetTrace(int backlog) {
		_lastBacklog = backlog;
		_processed.set(0);
		_elapsedMillis.set(0);
	}

	/**
	 * 尝试扩容或减容
	 */
	@Override
	public void tryScale() {

		var serverCount = _consumers.size();

		var currentBacklog = this.getMessageCount();

		if (currentBacklog == 0) {
			// 积压量等于0就要考虑是否减少服务了
			this.tryReduce(serverCount);
		} else {
			// 有积压，尝试扩容
			this.tryIncrease(serverCount, currentBacklog);
		}

		this.resetTrace(currentBacklog);
	}

	private void tryIncrease(int serverCount, int currentBacklog) {
		var maxConcurrency = _config.maxConcurrency();
		// 当maxConcurrency为0，表示不限制并发数
		if (maxConcurrency != 0 && serverCount >= maxConcurrency) // 服务个数已达到最大值
			return;

		// 如果当前积压量还大于上次积压量，证明积压量在上升
		if (currentBacklog >= _lastBacklog) {
			// 消息积压量在增加，增加一个服务分担
			this.increase();
		}
		// 以下代码不用执行
//		else if (currentBacklog < _lastBacklog) {
		// 积压量大于0，但是已经开始减少了，这时候什么也不用做
//		}
	}

	private void tryReduce(int serverCount) {

		if (serverCount == 1)
			return;

		// 1.获取当前时间间隔处理的消息总数
		var processedCount = _processed.get();

		if (processedCount == 0) {
			// 没有消息可以处理，直接减少服务
			this.reduce();
			return;
		}

		// 2.根据处理消息花费的总时间 / 处理的消息总数 得到一个服务处理1个消息需要的毫秒数
		// 注意，_elapsedMillis和_processed都是总数，相除即可，不需要再除以serverCount
		var processSpeed = (float) _elapsedMillis.get() / (float) processedCount;

		// 3.将间隔时间除以一个服务处理1个消息需要的毫秒数，得到1个服务在间隔时间内可以处理多少条消息，即：吞吐量
		var oneServerThroughput = _timeInterval * 1000 / processSpeed;

		// 如果减少一个服务，依然可以在间隔时间内处理当前数量的消息，那么就可以减少
		if ((serverCount - 1) * oneServerThroughput >= processedCount)
			this.reduce();

	}

	// #region 动态伸缩

	private static final ConcurrentLinkedQueue<IConsumer> _waiteDispose = new ConcurrentLinkedQueue<IConsumer>();

	private static void tryClearWaiteDispose() {
		if (_waiteDispose.size() > 0) {
			ListUtil.remove(_waiteDispose, (t) -> t.disposed());
		}
	}

	private static void schedulerAction() {
		tryClearWaiteDispose();

		var clusters = ConsumerClusterFactory.getAll();
		for (var cluster : clusters) {
			cluster.tryScale();
		}
	}

	private static final Timer _scheduler;

	private static final int _timeInterval = 10;

	static {
		_scheduler = new Timer(_timeInterval, TimeUnit.SECONDS, LoopType.FixedDelay);
		_scheduler.delay(10, () -> {
			schedulerAction();
		});
	}

	// #endregion

}
