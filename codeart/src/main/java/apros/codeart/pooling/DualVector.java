package apros.codeart.pooling;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import apros.codeart.TestSupport;
import apros.codeart.util.Algorithm;

final class DualVector implements AutoCloseable {

	// 在扩容的时候可以反复切换
	private final AtomicReferenceArray<AtomicResidentItemArray> _dualContainers = new AtomicReferenceArray<>(
			new AtomicResidentItemArray[2]);

	public AtomicResidentItemArray container() {
		return _dualContainers.getAcquire(_dualIndex.getAcquire());
	}

	@TestSupport
	AtomicResidentItemArray getA() {
		return _dualContainers.get(0);
	}

	@TestSupport
	AtomicResidentItemArray getB() {
		return _dualContainers.get(1);
	}

	private Pool<?> _pool;

	/**
	 * 池的当前容量
	 */
	private AtomicInteger _capacity;

	public int capacity() {
		return _capacity.getAcquire();
	}

	/**
	 * 池的初始容量
	 */
	private final int _initialCapacity;

	/**
	 * 池的最大容量
	 */
	private final int _maxCapacity;

	private AtomicInteger _pointer = new AtomicInteger(-1);

	@TestSupport
	public int pointer() {
		return _pointer.getAcquire();
	}

	private AtomicInteger _dualIndex = new AtomicInteger(0);

	/**
	 * 
	 * 用的哪个池的下标
	 * 
	 * @return
	 */
	@TestSupport
	public int dualIndex() {
		return _dualIndex.getAcquire();
	}

	public DualVector(Pool<?> pool, int initialCapacity, int maxCapacity) {
		_pool = pool;
		_initialCapacity = initialCapacity;
		_maxCapacity = maxCapacity;
		_capacity = new AtomicInteger(initialCapacity);
		initContainer();
	}

	private void initContainer() {
		var items = new AtomicResidentItemArray(_initialCapacity);

		for (var i = 0; i < items.length(); i++) {
			items.setRelease(i, new ResidentItem(_pool, this, i));
		}

		_dualContainers.setRelease(0, items);
	}

	private int getIndex() {
		return _pointer.updateAndGet(current -> (current + 1) % _capacity.getAcquire());
	}

	@TestSupport
	public int borrowedCount() {
		AtomicResidentItemArray container = this.container();
		int count = 0;
		for (var i = 0; i < container.length(); i++) {
			if (container.getAcquire(i).isBorrowed())
				count++;
		}
		return count;
	}

	/**
	 * 
	 * 尝试领取一个项
	 * 
	 * @return
	 */
	public IPoolItem tryClaim() {

		var index = getIndex();

		var item = this.container().getAcquire(index);

		if (item.tryClaim())
			return item;

		return null;
	}

	private AtomicBoolean _isDisposed = new AtomicBoolean(false);

	public boolean isDisposed() {
		return _isDisposed.getAcquire();
	}

	public void dispose() {
		if (this.isDisposed())
			return;

		_isDisposed.setRelease(true);
		var container = this.container();

		for (var i = 0; i < container.length(); i++) {
			var item = container.getAcquire(i);
			if (!item.isBorrowed()) // 对于借出去的项，归还时会自动释放
				item.dispose();
		}

		_dualContainers.setRelease(0, null);
		_dualContainers.setRelease(1, null);
	}

	/**
	 * 
	 * 扩容时，池会上锁，所以池片段不用考虑同一时间会有个多个线程进入该代码段
	 * 
	 * @return
	 * 
	 */
	boolean tryIncrease() {
		if (_initialCapacity == _maxCapacity)
			return false;

		if (_capacity.getAcquire() >= _maxCapacity)
			return false;

		var src = this.container();
		int oldCount = src.length();
		// oldCapacity >> 1 是将 oldCapacity 右移一位的结果，相当于 oldCapacity
		// 的一半。将这个值加到原始容量上，得到的就是新容量，即原始容量的150%。
		// 在这里是将片段数为原数量的1.5倍
		int newCount = Algorithm.increaseByOnePointFive(oldCount, _maxCapacity);

		int oldDualIndex = _dualIndex.getAcquire();
		var dest = new AtomicResidentItemArray(newCount); // 双片段组，一共也就2个
		_dualContainers.setRelease(((oldDualIndex + 1) % 2), dest);

		AtomicResidentItemArray.copy(src, dest, dest.length());

		for (var i = src.length(); i < newCount; i++) {
			// 补充增容的数据
			dest.setRelease(i, new ResidentItem(_pool, this, i));
		}

		// 注意，要先执行a,因为数据已经拷贝到新的对象容器了，切换到新的对象容器,如果这时候有外界来访问数据，哪怕b没有执行，那么取的数据范围也不会有危险
		// 执行完a后再执行b，新的容量生效，这样外部就可以获得扩展后的空间上存储的数据了

		// a.应用最新的对象容器
		_dualIndex.updateAndGet(current -> (current + 1) % 2);

		// b.新的容量
		_capacity.setRelease(newCount);

		// 释放老的对象容器
		_dualContainers.setRelease(oldDualIndex, null);

		return true;
	}

	/**
	 * 尝试减容
	 * 
	 * @return
	 */
	boolean tryDecrease() {

		if (_initialCapacity == _maxCapacity)
			return false;

		if (_capacity.getAcquire() == _initialCapacity)
			return false;

		var src = this.container();
		int oldCount = src.length();
		int newCount = Algorithm.reduceByOnePointFive(oldCount, _initialCapacity);
		int oldDualIndex = _dualIndex.getAcquire();

		var dest = new AtomicResidentItemArray(newCount); // 双片段组，一共也就2个
		_dualContainers.setRelease(((oldDualIndex + 1) % 2), dest);

		AtomicResidentItemArray.copy(src, dest, dest.length());

		// 注意，要先执行a,因为数据已经拷贝到新的矩阵池了，
		// 新的矩阵池的count小于老矩阵池，所以得先把数量设置
		// 这样就算b还没执行，而有人在借项，也是借老src里的矢量池的项，而且这些项都已经复制到b了，所以不会有危害

		// a.设置新的向量池容量
		_capacity.setRelease(newCount);

		// b.应用最新的向量池
		_dualIndex.updateAndGet(current -> (current + 1) % 2);

		for (var i = newCount; i < src.length(); i++) {
			var item = src.getAcquire(i);
			if (!item.isBorrowed())
				item.dispose();
		}

		// 释放向量池
		_dualContainers.setRelease(oldDualIndex, null);

		return true;
	}

	@Override
	public void close() {
		this.dispose();
	}

}
