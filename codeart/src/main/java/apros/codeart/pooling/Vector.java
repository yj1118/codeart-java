package apros.codeart.pooling;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import apros.codeart.TestSupport;
import apros.codeart.util.Algorithm;

final class Vector implements AutoCloseable {

	private final ConcurrentLinkedDeque<ResidentItem> _container = new ConcurrentLinkedDeque<>();

	/**
	 * 
	 */
	private final AtomicInteger _borrowCount = new AtomicInteger(0);

	/**
	 * 需要抛弃的项
	 */
	private final AtomicInteger _waitDisposeCount = new AtomicInteger(0);

	public int waitDisposeCount() {
		return _waitDisposeCount.get();
	}

	// 压栈
	void push(ResidentItem value) {

		while (true) {
			int current = _waitDisposeCount.get();
			if (current == 0) {
				_container.addFirst(value);
				_borrowCount.decrementAndGet();
				return;
			}

			if (_waitDisposeCount.compareAndSet(current, current - 1)) {
				// 需要销毁
				value.dispose();
				return;
			}
			// 如果 compareAndSet 失败，说明值已经被其他线程修改，继续循环
		}

	}

	// 弹栈
	private ResidentItem pop() {
		var item = _container.pollFirst();
		if (item != null) {
			_borrowCount.incrementAndGet();
		}
		return item;
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

	public Vector(Pool<?> pool, int initialCapacity, int maxCapacity) {
		_pool = pool;
		_initialCapacity = initialCapacity;
		_maxCapacity = maxCapacity;
		_capacity = new AtomicInteger(initialCapacity);
		initContainer();
	}

	private void initContainer() {

		for (var i = 0; i < _initialCapacity; i++) {
			_container.addFirst(new ResidentItem(_pool, this));
		}
	}

	@TestSupport
	public int borrowedCount() {
		return _borrowCount.get();
	}

	/**
	 * 
	 * 尝试领取一个项
	 * 
	 * @return
	 */
	public IPoolItem tryClaim() {
		return this.pop();
	}

	private AtomicBoolean _isDisposed = new AtomicBoolean(false);

	public boolean isDisposed() {
		return _isDisposed.getAcquire();
	}

	public void dispose() {
		if (this.isDisposed())
			return;

		_isDisposed.setRelease(true);

		while (true) {
			var item = this.pop();
			if (item == null)
				break;
			item.dispose();
		}

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

		int oldCount = _capacity.get();
		// oldCapacity >> 1 是将 oldCapacity 右移一位的结果，相当于 oldCapacity
		// 的一半。将这个值加到原始容量上，得到的就是新容量，即原始容量的150%。
		// 在这里是将片段数为原数量的1.5倍
		int newCount = Algorithm.increaseByOnePointFive(oldCount, _maxCapacity);

		for (var i = oldCount; i < newCount; i++) {
			// 补充增容的数据
			_container.addFirst(new ResidentItem(_pool, this));
		}

		// b.新的容量
		_capacity.setRelease(newCount);

		return true;
	}

	/**
	 * 尝试减容
	 * 
	 * @return
	 */
	boolean tryDecrease() {

		// 定值容量，不用减容
		if (_initialCapacity == _maxCapacity)
			return false;

		if (_capacity.getAcquire() == _initialCapacity)
			return false;

		int oldCount = _capacity.getAcquire();
		int newCount = Algorithm.reduceByOnePointFive(oldCount, _initialCapacity);

		var diff = oldCount - newCount;
		// 累加需要销毁的项的数量，在归还的时候会自动销毁多余的项
		_waitDisposeCount.addAndGet(diff);

		// 设置新的向量池容量,当归还项的时候，会自动销毁多余的项
		_capacity.setRelease(newCount);

		return true;
	}

	@Override
	public void close() {
		this.dispose();
	}

}
