package apros.codeart.pooling;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;

import apros.codeart.TestSupport;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.thread.Timer;

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
public class Pool<T> implements AutoCloseable {

	private final Function<Boolean, T> _itemFactory;
	/**
	 * 当项被返回到池中，会被回收者回收一次
	 */
	private final Consumer<T> _itemRecycler;
	private final Consumer<T> _itemDestroyer; // 当项被消除时，会使用该对象进行额外的销毁操作

	/**
	 * 当前使用的向量池在矩阵里的序号
	 */
	private AtomicInteger _pointer = new AtomicInteger(-1);

	private int next() {
		return _pointer.updateAndGet(current -> (current + 1) % _matrix.vectorCount());
	}

	void setPointer(int value) {
		// 由于后续调用了_pointer.updateAndGet，所以得用set方法确保可见性
		_pointer.set(value);
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

	private DualMatrix _matrix;

	public int vectorCapacity() {
		return _matrix.vectorCapacity();
	}

	private Timer _timer;

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
		_matrix = new DualMatrix(this, config.initialVectorCapacity(), config.maxVectorCapacity(),
				config.initialMatrixCapacity(), config.maxMatrixCapacity());

		_timer = new Timer(config.detectPeriod(), TimeUnit.SECONDS);
		_timer.delay(() -> _matrix.tryDecrease());
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
	private Vector claimVector() {
		var index = next(); // 取出下一个可用的矢量池坐标
		return _matrix.getVector(index);
	}

	public IPoolItem borrow() {
		var rb = this.claimVector();

		IPoolItem item = rb.tryClaim();

		if (item != null) {
			return item;
		}

		// 由于获得项失败了，表示池里对应的段已经满了，所以创建临时项给外界用
		item = TempItem.tryClaim(this);
		// 尝试扩容
		_matrix.tryIncrease();
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
		_timer.stop();
		_isDisposed.setRelease(true);
		_matrix.dispose();
	}

	@Override
	public void close() {
		this.dispose();
	}

	@TestSupport
	public static boolean isTemp(IPoolItem item) {
		return TypeUtil.is(item, TempItem.class);
	}

	@TestSupport
	public static record Layout(/**
								 * 缓存对象的总个数
								 */
	int capacity, /**
					 * 矢量池的数量
					 */
	int vectorCount, /**
						 * 当前有效的矢量池的下标
						 */
	int vectorPointer, /**
						 * 使用的是哪个矩阵池(也称为矢量池集合)，0是A，1是B
						 */
	int matrixDualIndex, VectorLayout[] matrixA, VectorLayout[] matrixB) {

	}

	@TestSupport
	public static record VectorLayout(
			/**
			 * 矢量池里存放的对象数量
			 */
			int capacity,
			/**
			 * 已借出的项的数量
			 */
			int borrowedCount,
			/**
			 * 等待销毁的项的数量
			 */
			int waitDisposeCount, /**
									 * 等待借出的项
									 */
			int waitBorrowCount) {

	}

	@TestSupport
	public static record StoreLayout(/**
										 * 存储数组存放的对象数量
										 */
	int capacity, /**
					 * 已借出的项的数量
					 */
	int borrowedCount) {

	}

	@TestSupport
	public Layout getLayout() {

		int capacity = _matrix.capacity();
		int vectorCount = _matrix.vectorCount();
		int vectorPointer = _pointer.getAcquire();
		int dualIndex = _matrix.dualIndex();

		VectorLayout[] matrixA = getMatrixLayout(_matrix.getA());
		VectorLayout[] matrixB = getMatrixLayout(_matrix.getB());

		return new Layout(capacity, vectorCount, vectorPointer, dualIndex, matrixA, matrixB);
	}

	@TestSupport
	private VectorLayout[] getMatrixLayout(AtomicVectorArray arr) {
		if (arr == null)
			return null;

		VectorLayout[] layouts = new VectorLayout[arr.length()];

		for (var i = 0; i < arr.length(); i++) {
			Vector dv = arr.getAcquire(i);

			var capacity = dv.capacity();
			var borrowed = dv.borrowedCount();

			var waitDisposeCount = dv.waitDisposeCount();
			var waitBorrowCount = dv.waitBorrowCount();

			var layout = new VectorLayout(capacity, borrowed, waitDisposeCount, waitBorrowCount);
			layouts[i] = layout;
		}

		return layouts;

	}

	@TestSupport
	private StoreLayout getStoreLayout(AtomicResidentItemArray store) {
		if (store == null)
			return null;

		var capacity = store.length();

		var borrowedCount = 0;

		for (var i = 0; i < store.length(); i++) {
			if (store.getAcquire(i).isBorrowed())
				borrowedCount++;
		}

		return new StoreLayout(capacity, borrowedCount);

	}

}
