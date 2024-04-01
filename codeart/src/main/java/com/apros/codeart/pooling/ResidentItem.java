package com.apros.codeart.pooling;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.TypeUtil.is;

import java.time.Instant;

/**
 * @param <T>
 */
/**
 * @param <T>
 */
final class ResidentItem<T> {
	private Pool<T> _owner;
	private T _item;
	private boolean _isBorrowed;
	private BorrowedItem<T> _borrowedItem;

	public ResidentItem(Pool<T> owner, T item) {
		_owner = owner;
		_item = item;
		_createTime = Instant.now();// 使用UtcNow使用世界时间，避免时区不同，本地时间不同
		_useCount = 0;
		_isCorrupted = false;
		_borrowedItem = new BorrowedItem<>(this);
	}

	public Pool<T> getOwner() {
		return _owner;
	}

	/// <summary>
	/// 项
	/// </summary>
	public T getItem() {
		return _item;
	}

	private boolean _isCorrupted;

	/**
	 * 获取一个值，表示该实例是否是损坏的，是否需要从所属的池中被移除
	 * 
	 * @return
	 */
	public boolean isCorrupted() {
		return _isCorrupted;
	}

	/**
	 * 设置实例是损坏的，需要从所属的池中被移除
	 */
	public void setCorrupted() {
		_isCorrupted = true;
	}

	private Instant _createTime;

	/// <summary>
	/// 获取该实例被创建的世界时间
	/// </summary>
	public Instant getCreateTime() {
		return _createTime;
	}

	public void setCreateTime(Instant createTime) {
		this._createTime = createTime;
	}

	private Instant _lastUsedTime;

	/**
	 * 获取该实例最后一次被使用的时间
	 * 
	 * @return
	 */
	public Instant getLastUsedTime() {
		return _lastUsedTime;
	}

	public void setLastUsedTime(Instant lastUsedTime) {
		this._lastUsedTime = lastUsedTime;
	}

	private int _useCount;

	/**
	 * 
	 * 获取该项被使用的次数
	 * 
	 * @return
	 */
	public int getUseCount() {
		return _useCount;
	}

	public boolean isReusable() {
		return is(this._item, IReusable.class);
	}

	/**
	 * 当项要被借出时，需要调用该方法，项不能重复借出
	 * 
	 * @param poolVersion 项被借出时，池的版本
	 * 
	 * @return 封装了借出的项，释放该项时，对象将返回到池中
	 */
	IPoolItem<T> borrow(int poolVersion) throws PoolingException {
		if (_isBorrowed)
			throw new PoolingException(strings("codeart", "RepeatBorrowingPoolItem", _owner.getClass().getName()));

		_isBorrowed = true;
		setPoolVersionWhenBorrowed(poolVersion);

		return _borrowedItem;
	}

	/**
	 * 项被借出时，池的版本
	 */
	private int _poolVersionWhenBorrowed;

	private void setPoolVersionWhenBorrowed(int poolVersion) {
		_poolVersionWhenBorrowed = poolVersion;
	}

	int getPoolVersionWhenBorrowed() {
		return _poolVersionWhenBorrowed;
	}

	/**
	 * 交还对象到所属的池中,当满足PoolConfig配置的条件时，项可能会被丢弃，并且相关的资源会被释放
	 */
	public void back() {
		if (!_isBorrowed)
			throw new PoolingException(strings("codeart", "CannotReturnPoolItem", _owner.getClass().getName()));
		_isBorrowed = false;
		_lastUsedTime = Instant.now(); // 更新最后一次使用的时间
		++_useCount;
		_owner.back(this);
	}
}
