package apros.codeart.pooling;

import static apros.codeart.i18n.Language.strings;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 我们要注意数值计算的原子性和可见性 原子性不代表可见性，所以需要用release和acquire模式
 */
final class ResidentItem implements IPoolItem {
	private Pool<?> _pool;
	private Vector _vector;
	private Object _item;
	private AtomicBoolean _isBorrowed;
	private AtomicBoolean _isDisposed;

	public ResidentItem(Pool<?> pool, Vector vector) {
		_pool = pool;
		_vector = vector;
		_item = pool.createItem(false);
		_isBorrowed = new AtomicBoolean(false);
		_isDisposed = new AtomicBoolean(false);
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

		if (_pool.isDisposed()) {
			this.dispose(); // 直接销毁
		} else {
			if (!_isBorrowed.getAcquire())
				throw new PoolingException(strings("apros.codeart", "CannotReturnPoolItem", _pool.getClass().getName()));
			_pool.clearItem(this); // 让池来处理清理工作
			_isBorrowed.setRelease(false); // 再标记已归还了
			_pool.borrowedDecrement(); // 借出数-1

			// 矢量池被销毁
			if (_vector.isDisposed()) {
				// 销毁
				this.dispose();
			} else {
				// 否则归还到池中
				_vector.push(this);
			}
		}

	}

	@Override
	public void close() {
		this.back();
	}

}
