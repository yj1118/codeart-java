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
	public void clear() throws Exception {
		_parent.back();
	}

	/**
	 * 该对象的关闭操作，就是清理操作，关闭后就返回到池中了，可以继续借出
	 */
	@Override
	public void close() throws Exception {
		this.close();
	}
}