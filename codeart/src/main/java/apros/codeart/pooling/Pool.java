package apros.codeart.pooling;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;

import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;

/**
 * 组成结构：
 * 
 * 1.双片段组（DualSegments）:
 * 
 * 对象池由1个双片段组（DualSegments）组成。（一个对象池一个双片段组，1对1关系）
 * 
 * 双片段组内部维护两个池段（PoolSegment）的集合(PoolSegment[])，(池段集合A和吃段集合B，A和B放在一个数组里：PoolSegment[2][])。
 * 
 * 同一时刻只有一个池段集合在工作：当A在工作时，B的内容为空，且闲置，当B在工作时，A的内容为空，且闲置。
 * 
 * 也就是说，池在工作的时候，有1个或者多个池段（PoolSegment）在活动，另外一组池段则在闲置，闲置的池段是null，不占用内存。
 * 
 * 2.池段（PoolSegment）：
 * 
 * 池段内部有2个容器，容器就是池项的集合（ResidentItem[]）。(容器a和容器b，a和b放在一个数组里：ResidentItem[2][])。
 * 
 * 同一时刻只有一个容器在工作：当a在工作时，b的内容为空，且闲置，当b在工作时，a的内容为空，且闲置。
 * 
 * @param <T>
 */
public class Pool<T> {

	private final Function<Boolean, T> _itemFactory;
	/**
	 * 当项被返回到池中，会被回收者回收一次
	 */
	private final Consumer<T> _itemRecycler;
	private final Consumer<T> _itemDestroyer; // 当项被消除时，会使用该对象进行额外的销毁操作

	private AtomicInteger _pointer = new AtomicInteger(-1);

	private int next() {
		return _pointer.updateAndGet(current -> (current + 1) % _dual.vectorCount());
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

	private DualMatrix _dual;

	public int vectorCapacity() {
		return _dual.vectorCapacity();
	}

	/**
	 * @param segmentSize  每个分段的大小
	 * @param segmentCount 分段数量initialSegmentCount
	 */
	public Pool(Class<T> itemType, PoolConfig config, Function<Boolean, T> itemFactory, Consumer<T> itemRecycler,
			Consumer<T> itemDestroyer) {
		_itemFactory = itemFactory;
		_itemRecycler = itemRecycler;
		_itemDestroyer = itemDestroyer;
		_itemDisposable = _itemDestroyer != null && itemType.isAssignableFrom(AutoCloseable.class);
		_dual = new DualMatrix(this, config.initialSegmentCapacity(), config.maxSegmentCapacity(),
				config.initialSegmentCount(), config.maxSegmentCount());
	}

	public Pool(Class<T> itemType, PoolConfig config, Function<Boolean, T> itemFactory) {
		this(itemType, config, itemFactory, null, null);
	}

	public Pool(Class<T> itemType, PoolConfig config, Function<Boolean, T> itemFactory, Consumer<T> itemRecycler) {
		this(itemType, config, itemFactory, itemRecycler, null);
	}

	/**
	 * 
	 * 使用轮询的方式领取一个可用的分段
	 * 
	 * @return
	 */
	private DualVector claimSegment() {
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

	private AtomicBoolean _isDisposed = new AtomicBoolean(false);

	public boolean isDisposed() {
		return _isDisposed.getAcquire();
	}

	/**
	 * 该方法一般测试环境中用，生产环境不会用到
	 */
	public void dispose() {
		if (this.isDisposed())
			return;
		_isDisposed.setRelease(true);
		_dual.dispose();
	}

}
