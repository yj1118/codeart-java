package apros.codeart.pooling;

import java.util.concurrent.atomic.AtomicReferenceArray;

class AtomicDualVectorArray {

	/*
	 * 
	 * set的性能比setRelease差一点，但是保证了编辑和读取的可见性，而setRelease只是保证了后续的getAcquire 的可见性。
	 * 
	 * set 方法 set 方法在 AtomicReferenceArray 中等同于对 volatile 变量的写操作。这意味着每次使用 set
	 * 方法写入一个元素时，写操作将立即对所有线程可见，同时确保写入操作前的所有操作都已经完成。因此，set 方法提供了很强的可见性保证，并且阻止了指令重排序。
	 * 
	 * 这种方法虽然保证了很高的数据一致性和可见性，但相对来说可能会因为强制每次都进行内存屏障而影响性能。
	 * 
	 * setRelease 方法 setRelease 方法则使用了更轻量级的内存屏障，专注于为后续的 getAcquire
	 * 调用提供必要的可见性保证。setRelease 仅确保在这个操作之前的所有写操作对随后通过 getAcquire 读取这个位置的线程可见。这就意味着
	 * setRelease 并不保证对所有线程的立即可见性，而是特定于与 getAcquire 配对使用时的线程间可见性。
	 * 
	 * 这种设计使得 setRelease 在不需要全局即时可见性，但依然需要一定线程间同步时，可以提供更好的性能。
	 * 
	 * 实际应用 在实际应用中，选择使用 set 还是 setRelease 应基于您的具体需求：
	 * 
	 * 如果您需要确保立即的跨线程可见性，并且对所有线程都立即可见，那么 set 是更好的选择。 如果您的应用场景中，写入操作主要是为了配合特定的获取操作（如
	 * getAcquire），并且您希望优化性能，那么使用 setRelease 可能更合适。 总之，set 提供了更全面的可见性保证，代价是可能的性能下降；而
	 * setRelease 在保证必要的可见性的同时，提供了性能上的优化。正确的选择取决于您对性能和数据一致性需求的平衡。
	 * 
	 */

	private AtomicReferenceArray<Vector> _items;

	public AtomicDualVectorArray(int count) {
		_items = new AtomicReferenceArray<Vector>(new Vector[count]);
	}

	public int length() {
		return _items.length();
	}

	public void setRelease(int i, Vector newValue) {
		_items.setRelease(i, newValue);
	}

	public Vector getAcquire(int i) {
		return _items.getAcquire(i);
	}

	/**
	 * 
	 * 将 {@src} 的 0-{@length}的内容复制到 {@dest}
	 * 
	 * @param src
	 * @param dest
	 * @param length
	 */
	public static void copy(AtomicDualVectorArray src, AtomicDualVectorArray dest, int length) {
		for (var i = 0; i < length; i++) {
			dest.setRelease(i, src.getAcquire(i));
		}
	}

}
