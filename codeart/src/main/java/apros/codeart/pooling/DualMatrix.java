package apros.codeart.pooling;

import static apros.codeart.i18n.Language.strings;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * 双矩阵
 */
class DualMatrix {

	// 双片段组模式，在扩容的时候可以反复切换
//	private final DualVector[][] _dualVectors = new DualVector[2][];

	private final AtomicReferenceArray<DualVector> _aVectors;

	private final AtomicReferenceArray<DualVector> _bVectors;

	private AtomicInteger _dualIndex = new AtomicInteger(0);

	private final int _initialVectorCapacity;

	private final int _maxVectorCapacity;

	private final int _initialVectorCount;

	private final int _maxVectorCount;

	private final Pool<?> _pool;

	/**
	 * 当前矩阵中矢量的大小
	 */
	private AtomicInteger _vectorCapacity;

	/**
	 * 矩阵池中拥有矢量池的数量
	 */
	private AtomicInteger _vectorCount;

	/**
	 * 最大可容纳的缓冲数量，0表示无限制
	 */
	private int _maxSize;

	public DualMatrix(Pool<?> pool, int initialSegmentCapacity, int maxSegmentCapacity, int initialSegmentCount,
			int maxSegmentCount) {
		_pool = pool;
		_initialVectorCapacity = initialSegmentCapacity;
		_maxVectorCapacity = maxSegmentCapacity;
		_initialVectorCount = initialSegmentCount;
		_maxVectorCount = maxSegmentCount;
		_maxSize = _maxVectorCount > 0 ? _maxVectorCapacity * _maxVectorCount : 0;
		_vectorCount = new AtomicInteger(_initialVectorCount);

		initSegments();
	}

	/**
	 * 构造时就创建分段，避免按需加载导致的并发控制，会增加额外的性能损耗
	 */
	private void initSegments() {
		var segments = _dualVectors[_dualIndex.getAcquire()] = new DualVector[_initialVectorCount];
		for (var i = 0; i < segments.length; i++) {
			segments[i] = new DualVector(_pool, _initialVectorCapacity, _maxVectorCapacity);
		}
	}

	/**
	 * 
	 * 矩阵池总容量
	 * 
	 * @return
	 */
	public int capacity() {
		return this.vectorCapacity() * this.vectorCount();
	}

	public AtomicReferenceArray<DualVector[] segments() {
		return _dualVectors[_dualIndex.getAcquire()];
	}

	/**
	 * 
	 * 当前池中每个矢量的容量
	 * 
	 * @return
	 */
	public int vectorCapacity() {
		return _vectorCapacity.getAcquire();
	}

	/**
	 * 
	 * 矩阵池中拥有矢量池的数量
	 * 
	 * @return
	 */
	public int vectorCount() {
		return _vectorCount.getAcquire();
	}

	private final Object _syncObject = new Object();

	boolean tryGrow() {
		// 如果借出项总数大于当前所有缓冲项的数量，那么扩容
		var size = this.capacity();
		if (_maxSize > 0 && size >= _maxSize) // 已达到最大限制，不扩容
			return false;

		if (_pool.borrowedCount() > size) {

			synchronized (_syncObject) {
				if (_pool.borrowedCount() > this.capacity()) {
					this.grow();
					return true;
				}
			}
		}
		return false;
	}

	boolean tryVectorGrow() {
		if (_vectorCapacity.getAcquire() >= _maxVectorCapacity)
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
		_vectorCapacity.setRelease(capacity); // 记录最新的分段里的容量

		return true;
	}

	/**
	 * 扩容
	 */
	void grow() {

		// 尝试矢量池扩容
		if (this.tryVectorGrow())
			return;

		// 如果矢量池的容量已达到上限，那么纵向扩容
		var src = this.segments();
		int oldCount = src.length;
		// oldCapacity >> 1 是将 oldCapacity 右移一位的结果，相当于 oldCapacity
		// 的一半。将这个值加到原始容量上，得到的就是新容量，即原始容量的150%。
		// 在这里是将片段数为原数量的1.5倍
		int newCount = oldCount + Math.max(1, oldCount >> 1);
		int oldDualIndex = _dualIndex.getAcquire();
		var dest = _dualVectors[((oldDualIndex + 1) % 2)] = new DualVector[newCount]; // 双片段组，一共也就2个

		System.arraycopy(src, 0, dest, 0, src.length);

		for (var i = src.length; i < newCount; i++) {
			// 补充增容的数据，注意此处的初始容量是当前的容量，因为有可能被扩容过
			dest[i] = new DualVector(_pool, _vectorCapacity.getAcquire(), _maxVectorCapacity);
		}

		// 注意，要先执行a,因为数据已经拷贝到新的分段组了，切换到新的分段组,如果这时候有外界来访问数据，哪怕b没有执行，那么取的数据范围也不会有危险
		// 执行完a后再执行b，新的容量生效，这样外部就可以获得扩展后的空间上存储的数据了

		// a.应用最新的分段组
		_dualIndex.updateAndGet(current -> (current + 1) % 2);

		// b.新的段落数量
		_vectorCount.setRelease(newCount);

		// 释放老片段组
		_dualVectors[oldDualIndex] = null;
	}

	boolean tryShrink() {

		// 当前矢量池的容量就是初始容量，不用减容
		if (this.vectorCapacity() == _initialVectorCapacity)
			return false;

		// 如果借出项总数小于矩阵池的总容量的3分之2，那么减容
		if (_pool.borrowedCount() < (this.capacity() / 1.5)) {

			synchronized (_syncObject) {
				if (_pool.borrowedCount() < (this.capacity() / 1.5)) {
					this.shrink();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 减容
	 */
	void shrink() {

		// 优先尝试缩减矩阵池
		if (this.tryShrinkMatrix())
			return;

	}

	private boolean tryShrinkMatrix() {

		// 如果矩阵容量已经达到最小值，那么不能减容
		if (this.vectorCount() == _initialVectorCount)
			return false;

		var src = this.segments();
		int oldCount = src.length;
		int newCount = Math.max(_initialVectorCount, (int) ((float) oldCount / 1.5F));
		int oldDualIndex = _dualIndex.getAcquire();

		var dest = _dualVectors[((oldDualIndex + 1) % 2)] = new DualVector[newCount]; // 双片段组，一共也就2个

		System.arraycopy(src, 0, dest, 0, dest.length);

		// 注意，要先执行a,因为数据已经拷贝到新的矩阵池了，
		// 新的矩阵池的count小于老矩阵池，所以得先把数量设置
		// 这样就算b还没执行，而有人在借项，也是借老src里的矢量池的项，而且这些项都已经复制到b了，所以不会有危害

		// a.设置新的段落矩阵池容量
		_vectorCount.setRelease(newCount);

		// b.应用最新的矩阵池
		_dualIndex.updateAndGet(current -> (current + 1) % 2);

		// 释放被减容的矩阵池资源：把多余的矢量池释放
		for (var i = newCount; i < src.length; i++) {
			src[i].dispose();
		}

		// 释放老矩阵池
		_dualVectors[oldDualIndex] = null;
		return true;
	}

	public void dispose() {
		var segments = this.segments();
		for (var segment : segments) {
			segment.dispose();
		}
		_dualVectors[0] = null;
		_dualVectors[1] = null;
	}

}
