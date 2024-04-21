package apros.codeart.pooling;

final class TempItem implements IPoolItem {
	private Pool<?> _owner;
	private Object _item;

	private TempItem(Pool<?> owner) {
		_owner = owner;
		_item = owner.createItem(true);
	}

	/// <summary>
	/// 项
	/// </summary>
	@SuppressWarnings("unchecked")
	public <T> T getItem() {
		return (T) _item;
	}

	public static TempItem tryClaim(Pool<?> owner) {
		owner.borrowedIncrement(); // 借出数+1
		return new TempItem(owner);
	}

	@Override
	public void back() {
		// 临时项直接销毁
		_owner.disposeItem(this);
		_owner.borrowedDecrement(); // 借出数-1
	}

	@Override
	public void close() {
		this.back();
	}

}