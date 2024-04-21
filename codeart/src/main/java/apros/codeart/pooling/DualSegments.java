package apros.codeart.pooling;

import static apros.codeart.i18n.Language.strings;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 双片段组
 */
class DualSegments {

	// 双片段组模式，在扩容的时候可以反复切换
	private final DualPoolSegment[][] _dualSegments = new DualPoolSegment[2][];

	private AtomicInteger _dualIndex = new AtomicInteger(0);

	private final int _initialSegmentCapacity;

	private final int _maxSegmentCapacity;

	private final int _initialSegmentCount;

	private final int _maxSegmentCount;

	private final Pool<?> _pool;

	/**
	 * 当前池中每个片段的容量
	 */
	private AtomicInteger _segmentCapacity;

	/**
	 * 当前池中的片段数量
	 */
	private AtomicInteger _segmentCount;

	/**
	 * 最大可容纳的缓冲数量，0表示无限制
	 */
	private int _maxSize;

	public DualSegments(Pool<?> pool, int initialSegmentCapacity, int maxSegmentCapacity, int initialSegmentCount,
			int maxSegmentCount) {
		_pool = pool;
		_initialSegmentCapacity = initialSegmentCapacity;
		_maxSegmentCapacity = maxSegmentCapacity;
		_initialSegmentCount = initialSegmentCount;
		_maxSegmentCount = maxSegmentCount;
		_maxSize = _maxSegmentCount > 0 ? _maxSegmentCapacity * _maxSegmentCount : 0;
		_segmentCount = new AtomicInteger(_initialSegmentCount);

		initSegments();
	}

	private void initSegments() {
		var segments = _dualSegments[_dualIndex.getAcquire()] = new DualPoolSegment[_initialSegmentCount];
		for (var i = 0; i < segments.length; i++) {
			segments[i] = new DualPoolSegment(_pool, _initialSegmentCapacity, _maxSegmentCapacity);
		}
	}

	/**
	 * 
	 * 池可以容纳元素的总大小
	 * 
	 * @return
	 */
	public int size() {
		return this.segmentCapacity() * this.segmentCount();
	}

	public DualPoolSegment[] segments() {
		return _dualSegments[_dualIndex.getAcquire()];
	}

	/**
	 * 
	 * 当前池中每个片段的容量
	 * 
	 * @return
	 */
	public int segmentCapacity() {
		return _segmentCapacity.getAcquire();
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

	private final Object _syncObject = new Object();

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

	boolean trySegmentGrow() {
		if (_segmentCapacity.getAcquire() >= _maxSegmentCapacity)
			return false;

		var segments = this.segments();

		for (var i = 0; i < segments.length; i++) {
			var seg = segments[i];
			if (!seg.tryGrow()) {
				// 所有的分段是统一调整大小的，所以不会有这种情况
				throw new IllegalStateException(strings("codeart", "UnknownException"));
			}
		}

		var capacity = segments[0].capacity();
		_segmentCapacity.setRelease(capacity); // 记录最新的分段里的容量

		return true;
	}

	/**
	 * 扩容
	 */
	void grow() {

		// 尝试横向扩容，也就是每个分段扩容大小
		if (this.trySegmentGrow())
			return;

		// 如果横向已满，那么纵向扩容
		var src = this.segments();
		int oldCount = src.length;
		// oldCapacity >> 1 是将 oldCapacity 右移一位的结果，相当于 oldCapacity
		// 的一半。将这个值加到原始容量上，得到的就是新容量，即原始容量的150%。
		// 在这里是将片段数为原数量的1.5倍
		int newCount = oldCount + Math.max(1, oldCount >> 1);
		int oldDualIndex = _dualIndex.getAcquire();
		var dest = _dualSegments[((oldDualIndex + 1) % 2)] = new DualPoolSegment[newCount]; // 双片段组，一共也就2个

		System.arraycopy(src, 0, dest, 0, src.length);

		for (var i = src.length; i < newCount; i++) {
			// 补充增容的数据，注意此处的初始容量是当前的容量，因为有可能被扩容过
			dest[i] = new DualPoolSegment(_pool, _segmentCapacity.getAcquire(), _maxSegmentCapacity);
		}

		// 注意，要先执行a,因为数据已经拷贝到新的分段组了，切换到新的分段组,如果这时候有外界来访问数据，哪怕b没有执行，那么取的数据范围也不会有危险
		// 执行完a后再执行b，新的容量生效，这样外部就可以获得扩展后的空间上存储的数据了

		// a.应用最新的分段组
		_dualIndex.updateAndGet(current -> (current + 1) % 2);

		// b.新的段落数量
		_segmentCount.setRelease(newCount);

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
