package apros.codeart.pooling;

import java.util.concurrent.atomic.AtomicInteger;

final class PoolSegment {

	// 在扩容的时候可以反复切换
	private final ResidentItem[][] _dualContainers = new ResidentItem[2][];

	public ResidentItem[] container() {
		return _dualContainers[_dualIndex.getAcquire()];
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

	private AtomicInteger _dualIndex = new AtomicInteger(0);

	public PoolSegment(Pool<?> pool, int initialCapacity, int maxCapacity) {
		_pool = pool;
		_initialCapacity = initialCapacity;
		_maxCapacity = maxCapacity;
		_capacity = new AtomicInteger(initialCapacity);
		initContainer();
	}

	private void initContainer() {
		var items = _dualContainers[_dualIndex.getAcquire()] = new ResidentItem[_initialCapacity];

		for (var i = 0; i < items.length; i++) {
			items[i] = new ResidentItem(_pool);
		}
	}

	private int getIndex() {
		return _pointer.updateAndGet(current -> (current + 1) % _capacity.getAcquire());
	}

	/**
	 * 
	 * 尝试领取一个项
	 * 
	 * @return
	 */
	public IPoolItem tryClaim() {

		var index = getIndex();

		var item = this.container()[index];

		if (item.tryClaim())
			return item;

		return null;
	}

	public void dispose() {
		for (var item : this.container()) {
			if (!item.isBorrowed()) // 对于借出去的项，归还时会自动释放
				item.dispose();
		}

		_dualContainers[0] = null;
		_dualContainers[1] = null;
	}

	/**
	 * 
	 * 扩容时，池会上锁，所以池片段不用考虑同一时间会有个多个线程进入该代码段
	 * 
	 * @return
	 * 
	 */
	boolean tryGrow() {
		if (_initialCapacity == _maxCapacity)
			return false;

		if (_capacity.getAcquire() >= _maxCapacity)
			return false;

		var src = this.container();
		int oldCount = src.length;
		// oldCapacity >> 1 是将 oldCapacity 右移一位的结果，相当于 oldCapacity
		// 的一半。将这个值加到原始容量上，得到的就是新容量，即原始容量的150%。
		// 在这里是将片段数为原数量的1.5倍
		int newCount = oldCount + Math.max(1, oldCount >> 1);
		newCount = Math.min(newCount, _maxCapacity); // 确保不能超过maxCapacity

		int oldDualIndex = _dualIndex.getAcquire();
		var dest = _dualContainers[((oldDualIndex + 1) % 2)] = new ResidentItem[newCount]; // 双片段组，一共也就2个

		System.arraycopy(src, 0, dest, 0, dest.length);

		for (var i = src.length; i < newCount; i++) {
			// 补充增容的数据
			dest[i] = new ResidentItem(_pool);
		}

		// 注意，要先执行a,因为数据已经拷贝到新的对象容器了，切换到新的对象容器,如果这时候有外界来访问数据，哪怕b没有执行，那么取的数据范围也不会有危险
		// 执行完a后再执行b，新的容量生效，这样外部就可以获得扩展后的空间上存储的数据了

		// a.应用最新的对象容器
		_dualIndex.updateAndGet(current -> (current + 1) % 2);

		// b.新的容量
		_capacity.setRelease(newCount);

		// 释放老的对象容器
		_dualContainers[oldDualIndex] = null;

		return true;
	}

}
