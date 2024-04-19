package apros.codeart.pooling;

import static apros.codeart.i18n.Language.strings;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @param <T>
 */
/**
 * @param <T>
 */
final class ResidentItem implements IPoolItem {
	private IPoolSegment _owner;
	private Object _item;
	private AtomicBoolean _isBorrowed;

	public ResidentItem(IPoolSegment owner, Object item) {
		_owner = owner;
		_item = item;
		_isBorrowed = new AtomicBoolean(false);
	}

	public IPoolSegment getOwner() {
		return _owner;
	}

	/// <summary>
	/// 项
	/// </summary>
	@SuppressWarnings("unchecked")
	public <T> T getItem() {
		return (T) _item;
	}

	public boolean tryClaim() {
		// 如果_isBorrowed是true，那么立马变为false，并且返回true，表示借成功了
		if (_isBorrowed.compareAndSet(true, false))
			return true;
		return false;
	}

	/**
	 * 该对象的关闭操作，就是清理操作，关闭后就返回到池中了，可以继续借出
	 */
	@Override
	public void close() {
		if (!_isBorrowed.get())
			throw new PoolingException(strings("codeart", "CannotReturnPoolItem", _owner.getClass().getName()));
		_owner.clear(this); // 让池来处理清理工作
		_isBorrowed.set(false); // 再标记已归还了
	}

}
