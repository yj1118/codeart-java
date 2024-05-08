package apros.codeart.rabbitmq.rpc;

import java.time.Duration;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import apros.codeart.mq.rpc.server.IRPCHandler;
import apros.codeart.mq.rpc.server.IServer;
import apros.codeart.rabbitmq.RabbitBus;
import apros.codeart.util.ListUtil;
import apros.codeart.util.thread.Timer;
import apros.codeart.util.thread.Timer.LoopType;

class RPCServerCluster implements IServer, AutoCloseable {

	private IRPCHandler _handler;

	private final String _queue;

	private final String _name;

	public String getName() {
		return _name;
	}

	private final ConcurrentLinkedQueue<RPCServer> _items = new ConcurrentLinkedQueue<RPCServer>();

	private final LinkedList<RPCServer> _waiteDispose = new LinkedList<RPCServer>();

	private final RPCServerConfig _config;

	public RPCServerCluster(String method) {
		_name = method;
		_config = RPCConfig.getServerConfig(method);
		_queue = RPCConfig.getServerQueue(method);
	}

	public void initialize(IRPCHandler handler) {
		_handler = handler;
	}

	/**
	 * 
	 * 队列中有多少没有处理的消息数量
	 * 
	 * @return
	 */
	public int getMessageCount() {
		return RabbitBus.getMessageCount(RPCConfig.ServerPolicy, _queue);
	}

	private void increase() {
		var server = new RPCServer(this, _queue, _handler);
		server.open();
		_items.add(server);
	}

	private void reduce() {
		// 减少一个服务
		var item = _items.poll();
		if (item != null) {
			item.close(); // 关闭服务
			// 由于关闭服务是延迟的，需要等待下次处理一个请求后再关闭，所以这里得用_waiteDispose持有它得引用，否则被jvm回收了，引起BUG
			_waiteDispose.add(item);
		}
	}

	public void open() {
		this.increase();
	}

	@Override
	public void close() {
		for (var s : _items)
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
	void messagesProcessed(Duration elapsed) {
		_processed.incrementAndGet();
		_elapsedMillis.addAndGet(elapsed.toMillis());
	}

	private void resetTrace(int backlog) {
		_lastBacklog = backlog;
		_processed.set(0);
	}

	private void tryClearWaiteDispose() {
		if (_waiteDispose.size() > 0) {
			ListUtil.remove(_waiteDispose, (t) -> t.disposed());
		}
	}

	/**
	 * 尝试扩容或减容
	 */
	private void tryScale() {

		tryClearWaiteDispose();

		var serverCount = _items.size();

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

	private static final Timer _scheduler;

	private static final int _timeInterval = 10;

	static {
		_scheduler = new Timer(_timeInterval, TimeUnit.SECONDS, LoopType.FixedDelay);
		_scheduler.delay(10, () -> {
			var servers = RPCServerFactory.Instance.getAll();
			for (var srv : servers) {
				@SuppressWarnings("resource")
				var c = (RPCServerCluster) srv;
				c.tryScale();
			}
		});
	}

	// #endregion

}
