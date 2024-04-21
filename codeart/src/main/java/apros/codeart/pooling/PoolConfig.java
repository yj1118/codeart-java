package apros.codeart.pooling;

import apros.codeart.util.ArgumentAssert;

public class PoolConfig {

	private int _initialSegmentCapacity;

	/**
	 * 
	 * 分段容量的初始值
	 * 
	 * @return
	 */
	public int initialSegmentCapacity() {
		return _initialSegmentCapacity;
	}

	private int _maxSegmentCapacity;

	/**
	 * 
	 * 分段容量的最大值（扩容后的上限值）,不能为0，因为分段容量就是一维数组，不能没有限制扩大
	 * 
	 * @return
	 */
	public int maxSegmentCapacity() {
		return _maxSegmentCapacity;
	}

	private int _initialSegmentCount;

	/**
	 * 
	 * 初始分段数量(为了降低配置的复杂性，该值默认为2，且不需要更改)
	 * 
	 * @return
	 */
	public int initialSegmentCount() {
		return _initialSegmentCount;
	}

	private int _maxSegmentCount;

	/**
	 * 
	 * 分段数量最大值，为0表示不限制
	 * 
	 * @return
	 */
	public int maxSegmentCount() {
		return _maxSegmentCount;
	}

	private PoolConfig(int initialSegmentCapacity, int maxSegmentCapacity, int initialSegmentCount,
			int maxSegmentCount) {

		ArgumentAssert.lessThanOrEqualZero(_initialSegmentCapacity, "initialSegmentCapacity");
		ArgumentAssert.lessThanOrEqualZero(_maxSegmentCapacity, "maxSegmentCapacity");
		ArgumentAssert.lessThanOrEqualZero(_initialSegmentCount, "initialSegmentCount");

		_initialSegmentCapacity = initialSegmentCapacity;
		_maxSegmentCapacity = maxSegmentCapacity;
		_initialSegmentCount = initialSegmentCount;
		_maxSegmentCount = maxSegmentCount;
	}

	public PoolConfig(int initialSegmentCapacity, int maxSegmentCapacity, int maxSegmentCount) {
		this(initialSegmentCapacity, maxSegmentCapacity, 2, maxSegmentCount);
	}

	public PoolConfig(int initialSegmentCapacity, int maxSegmentCapacity) {
		this(initialSegmentCapacity, maxSegmentCapacity, 2, 0);
	}

}
