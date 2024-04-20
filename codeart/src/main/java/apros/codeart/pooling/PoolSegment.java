package apros.codeart.pooling;

import java.util.concurrent.atomic.AtomicInteger;

final class PoolSegment {

	private ResidentItem[] _container;

	private Pool<?> _pool;

	/**
	 * 池的容量
	 */
	private final int _capacity;

	private AtomicInteger _pointer = new AtomicInteger(-1);

	public PoolSegment(Pool<?> pool, int capacity) {
		_pool = pool;
		_capacity = capacity;
		_container = createContainer();
	}

	private ResidentItem[] createContainer() {

		var items = new ResidentItem[_capacity];

		for (var i = 0; i < items.length; i++) {
			items[i] = new ResidentItem(_pool);
		}

		return items;
	}

	private int getIndex() {
		return _pointer.updateAndGet(current -> (current + 1) % _capacity);
	}

	/**
	 * 
	 * 尝试领取一个项
	 * 
	 * @return
	 */
	public IPoolItem tryClaim() {

		var index = getIndex();

		var item = _container[index];

		if (item.tryClaim())
			return item;

		return null;
	}

	public void dispose() {
		for (var item : _container) {
			if (!item.isBorrowed()) // 对于借出去的项，归还时会自动释放
				item.dispose();
		}
	}
}
