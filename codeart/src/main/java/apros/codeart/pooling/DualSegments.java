package apros.codeart.pooling;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 双片段组
 */
class DualSegments {

	// 双片段组模式，在扩容的时候可以反复切换
	private final PoolSegment[][] _dualSegments = new PoolSegment[2][];

	private AtomicInteger _dualIndex = new AtomicInteger(0);

	private final int _minSegmentCount;

	private final int _maxSegmentCount;

	private final int _maxSize;

	private final int _segmentCapacity;

	private final Pool<?> _pool;

	/**
	 * 当前池中的片段数量
	 */
	private AtomicInteger _segmentCount;

	public DualSegments(Pool<?> pool, int segmentCapacity, int minSegmentCount, int maxSegmentCount) {
		_pool = pool;
		_segmentCapacity = segmentCapacity;
		_minSegmentCount = minSegmentCount;
		_maxSegmentCount = maxSegmentCount;
		_maxSize = _maxSegmentCount > 0 ? _maxSegmentCount * maxSegmentCount : 0;
		_segmentCount = new AtomicInteger(_minSegmentCount);

		initSegments();
	}

	private void initSegments() {
		var segments = _dualSegments[_dualIndex.getAcquire()] = new PoolSegment[_minSegmentCount];
		for (var i = 0; i < segments.length; i++) {
			segments[i] = new PoolSegment(_pool, _segmentCapacity);
		}
	}

	/**
	 * 
	 * 池可以容纳元素的总大小
	 * 
	 * @return
	 */
	public int size() {
		return _segmentCapacity * this.segmentCount();
	}

	public PoolSegment[] segments() {
		return _dualSegments[_dualIndex.getAcquire()];
	}

	/**
	 * 
	 * 片段数量
	 * 
	 * @return
	 */
	public int segmentCount() {
		return _segmentCount.getAcquire();
	}

	private Object _syncObject = new Object();

	boolean tryGrow() {
		// 如果借出项总数大于当前所有缓冲项的数量，那么扩容
		var size = this.size();
		if (_maxSize > 0 && size >= _maxSize) // 已达到最大限制，不扩容
			return false;

		if (_pool.borrowedCount() > size) {

			synchronized (_syncObject) {
				if (_pool.borrowedCount() > this.size()) {
					this.grow();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 扩容
	 */
	void grow() {
		var srcSegments = this.segments();
		int oldCount = srcSegments.length;
		// oldCapacity >> 1 是将 oldCapacity 右移一位的结果，相当于 oldCapacity
		// 的一半。将这个值加到原始容量上，得到的就是新容量，即原始容量的150%。
		// 在这里是将片段数为原数量的1.5倍
		int newCount = oldCount + Math.max(1, oldCount >> 1);
		int oldDualIndex = _dualIndex.getAcquire();
		var destSegments = _dualSegments[((oldDualIndex + 1) % 2)] = new PoolSegment[newCount]; // 双片段组，一共也就2个

		System.arraycopy(srcSegments, 0, destSegments, 0, srcSegments.length);

		for (var i = srcSegments.length; i < newCount; i++) {
			// 补充增容的数据
			destSegments[i] = new PoolSegment(_pool, _segmentCapacity);
		}

		// 新的段落数量
		_segmentCount.setRelease(newCount);

		// 应用最新的分段组
		_dualIndex.updateAndGet(current -> (current + 1) % 2);

		// 释放老片段组
		_dualSegments[oldDualIndex] = null;
	}

	public void dispose() {
		var segments = this.segments();
		for (var segment : segments) {
			segment.dispose();
		}
		_dualSegments[0] = null;
		_dualSegments[1] = null;
	}

}
