package apros.codeart.pooling;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;

import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;

public class Pool<T> {

	private final Function<Boolean, T> _itemFactory;
	/**
	 * 当项被返回到池中，会被回收者回收一次
	 */
	private final Consumer<T> _itemRecycler;
	private final Consumer<T> _itemDestroyer; // 当项被消除时，会使用该对象进行额外的销毁操作

	private AtomicInteger _pointer = new AtomicInteger(-1);

	private int next() {
		return _pointer.updateAndGet(current -> (current + 1) % _dual.segmentCount());
	}

	private boolean _itemDisposable;

	public boolean itemDisposable() {
		return _itemDisposable;
	}

	private LongAdder _borrowedCount = new LongAdder();

	public int borrowedCount() {
		return _borrowedCount.intValue();
	}

	void borrowedIncrement() {
		_borrowedCount.increment();
	}

	void borrowedDecrement() {
		_borrowedCount.decrement();
	}

	private DualSegments _dual;

	/**
	 * @param segmentSize  每个分段的大小
	 * @param segmentCount 分段数量initialSegmentCount
	 */
	public Pool(Class<T> itemType, int segmentCapacity, int minSegmentCount, int maxSegmentCount,
			Function<Boolean, T> itemFactory, Consumer<T> itemRecycler, Consumer<T> itemDestroyer) {
		_itemFactory = itemFactory;
		_itemRecycler = itemRecycler;
		_itemDestroyer = itemDestroyer;
		_itemDisposable = _itemDestroyer != null && itemType.isAssignableFrom(AutoCloseable.class);
		_dual = new DualSegments(this, segmentCapacity, minSegmentCount, maxSegmentCount);
	}

	public Pool(Class<T> itemType, int segmentCapacity, int minSegmentCount, Function<Boolean, T> itemFactory) {
		this(itemType, segmentCapacity, minSegmentCount, 0, itemFactory, null, null);
	}

	public Pool(Class<T> itemType, int segmentCapacity, int minSegmentCount, Function<Boolean, T> itemFactory,
			Consumer<T> itemRecycler) {
		this(itemType, segmentCapacity, minSegmentCount, 0, itemFactory, itemRecycler, null);
	}

	public Pool(Class<T> itemType, int segmentCapacity, Function<Boolean, T> itemFactory) {
		this(itemType, segmentCapacity, 2, 0, itemFactory, null, null);
	}

	public Pool(Class<T> itemType, Function<Boolean, T> itemFactory) {
		this(itemType, 100, 2, 0, itemFactory, null, null);
	}

	/**
	 * 构造时就创建分段，避免按需加载导致的并发控制，会增加额外的性能损耗
	 */

	/**
	 * 
	 * 使用轮询的方式领取一个可用的分段
	 * 
	 * @return
	 */
	private PoolSegment claimSegment() {
		var index = next(); // 取出下一个可用的分段坐标
		return _dual.segments()[index];
	}

	public IPoolItem borrow() {
		var rb = this.claimSegment();

		IPoolItem item = rb.tryClaim();

		if (item != null) {
			return item;
		}

		// 由于获得项失败了，表示池里对应的段已经满了，所以创建临时项给外界用
		item = TempItem.tryClaim(this);
		_dual.tryGrow();
		return item;
	}

	public <R> R using(Function<T, R> action) {
		try (IPoolItem item = borrow()) {
			return action.apply(item.getItem());
		}
	}

	/**
	 * 使用池中的项
	 * 
	 * @param action
	 */
	public void using(Consumer<T> action) {
		try (IPoolItem item = borrow()) {
			action.accept(item.getItem());
		}
	}

	Object createItem(Boolean isTempItem) {
		return _itemFactory.apply(isTempItem);
	}

	void clearItem(IPoolItem item) {
		if (_itemRecycler != null) {
			_itemRecycler.accept(item.getItem());
		}

		var reusableObject = TypeUtil.as(item.getItem(), IReusable.class);
		if (reusableObject != null)
			reusableObject.clear();
	}

	/**
	 * @param item
	 */
	void disposeItem(IPoolItem item) {
		if (!this.itemDisposable())
			return;

		try {
			if (_itemDestroyer != null)
				_itemDestroyer.accept(item.getItem());

			var disposableObject = TypeUtil.as(item.getItem(), AutoCloseable.class);
			if (disposableObject != null)
				disposableObject.close();
		} catch (Exception e) {
			throw new PoolingException(Language.strings("codeart", "DisposePoolItemFailed", this.getClass().getName()),
					e);
		}
	}

	private boolean _isDisposed = false;

	public boolean isDisposed() {
		return _isDisposed;
	}

	/**
	 * 该方法一般测试环境中用，生产环境不会用到
	 */
	public void dispose() {
		_isDisposed = true;
		_dual.dispose();
	}

}
