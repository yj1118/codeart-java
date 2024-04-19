package apros.codeart.pooling;

final class TempItem implements IPoolItem {
	private IPool _owner;
	private Object _item;

	public TempItem(IPool owner, Object item) {
		_owner = owner;
		_item = item;
	}

	public IPool getOwner() {
		return _owner;
	}

	/// <summary>
	/// 项
	/// </summary>
	@SuppressWarnings("unchecked")
	public <T> T getItem() {
		return (T) _item;
	}

	/**
	 * 该对象的关闭操作，就是清理操作，关闭后就返回到池中了，可以继续借出
	 */
	@Override
	public void close() {
		_owner.clear(this); // 让池来处理清理工作
	}

}