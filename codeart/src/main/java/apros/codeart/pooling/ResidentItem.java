package apros.codeart.pooling;

import static apros.codeart.i18n.Language.strings;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 我们要注意数值计算的原子性和可见性 原子性不代表可见性，所以需要用release和acquire模式
 */
final class ResidentItem implements IPoolItem {
	private Pool<?> _pool;
	private DualVector _vector;
	private Object _item;
	private AtomicBoolean _isBorrowed;
	private AtomicBoolean _isDisposed;

	private final int _index;

	/**
	 * 
	 * 项在池中的矢量位置
	 * 
	 * @return
	 */
	public int index() {
		return _index;
	}

	public ResidentItem(Pool<?> pool, DualVector vector, int index) {
		_pool = pool;
		_vector = vector;
		_item = pool.createItem(false);
		_isBorrowed = new AtomicBoolean(false);
		_isDisposed = new AtomicBoolean(false);
		_index = index;
	}

	public boolean isBorrowed() {
		return _isBorrowed.getAcquire();
	}

	/// <summary>
	/// 项
	/// </summary>
	@SuppressWarnings("unchecked")
	public <T> T getItem() {
		return (T) _item;
	}

	public boolean tryClaim() {
		// 如果_isBorrowed是false，那么立马变为true，并且返回上一个状态值false，表示借成功了
		// 否则表示还在被别的线程使用中
		// 使用compareAndExchange是因为它同时保证了 acquire 和 release 语义
		// acquire: 确保当前读到的一定是最新数据
		// release: 确保当前写入的数据，对其他线程立即可见
		if (_isBorrowed.compareAndExchange(false, true) == false) {
			_pool.borrowedIncrement(); // 借出数+1
			return true;
		}

		return false;
	}

	public void dispose() {
		if (_isDisposed.getAcquire())
			return;

		_isDisposed.setRelease(true);
		_pool.disposeItem(this); // 销毁项
	}

	/**
	 * 该对象的关闭操作，就是清理操作，关闭后就返回到池中了，可以继续借出
	 */
	@Override
	public void back() {

		// 如果项具备销毁的能力，并且版本号不同，那么就销毁，不再使用
		boolean itemDisposable = _pool.itemDisposable();
		if (itemDisposable && (_pool.isDisposed())) {
			this.dispose(); // 直接销毁
		} else {
			if (!_isBorrowed.getAcquire())
				throw new PoolingException(strings("codeart", "CannotReturnPoolItem", _pool.getClass().getName()));
			_pool.clearItem(this); // 让池来处理清理工作
			_isBorrowed.setRelease(false); // 再标记已归还了
			_pool.borrowedDecrement(); // 借出数-1

			if (itemDisposable && (_vector.isDisposed() || this.index() >= _pool.vectorCapacity())) {
				// 项所在的位置比矢量池的容量还大，那证明池已经被缩减容量了，需要销毁该项
				this.dispose();
			}

		}

	}

	@Override
	public void close() {
		this.back();
	}

}
