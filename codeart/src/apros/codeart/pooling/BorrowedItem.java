package apros.codeart.pooling;

/**
 * 封装被借出的项
 * 
 * @param <T>
 */
final class BorrowedItem<T> implements IPoolItem<T> {

	private final ResidentItem<T> _parent;

	public BorrowedItem(ResidentItem<T> parent) {
		_parent = parent;
	}

	public Pool<T> getOwner() {
		return _parent.getOwner();
	}

	public T getItem() {
		return _parent.getItem();
	}

	public boolean isCorrupted() {
		return _parent.isCorrupted();
	}

	@Override
	public void setCorrupted() {
		_parent.setCorrupted();
	}

	@Override
	public void clear() throws PoolingException {
		_parent.back();
	}
}