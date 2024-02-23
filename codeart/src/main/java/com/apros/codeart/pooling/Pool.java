package com.apros.codeart.pooling;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.Util.as;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

import com.apros.codeart.util.Action1;
import com.apros.codeart.util.Func;
import com.apros.codeart.util.Func1;
import com.apros.codeart.util.Func2;

public final class Pool<T> implements AutoCloseable {

	/// <summary>
	/// 同步对象
	/// </summary>
	private final Object _syncRoot = new Object();
	private final Func<T> _itemFactory;
	/**
	 * 返回true，表示继续使用该项，返回false表示抛弃项
	 */
	private final Func2<T, PoolItemPhase, Boolean> _itemFilter;
	private final Action1<T> _itemDestroyer; // 当项被消除时，会使用该对象进行额外的销毁操作
	private IPoolContainer<ResidentItem<T>> _container;
	private boolean _isDisposed;
	private int _poolVersion;

	private final PoolFetchOrder _fetchOrder;

	/**
	 * 设置或获取在同一个时间内，最大能够借出的项的数量。
	 * 
	 * 如果线程池中借出的项数量达到该值，那么下次在借用项时，调用线程将被阻塞，直到有项被返回到线程池中。
	 * 
	 * 如果该值小于或者等于0，那么项会被马上借给调用线程，默认值是0（无限）
	 */
	private final int _loanCapacity;

	/**
	 * 获取或设置池中可容纳的最大项数量。
	 * 
	 * 当项被返回到池中时，如果池的容量已达到最大值，那么该项将被抛弃。
	 * 
	 * 如果该值小于或等于0，代表无限制
	 */
	private final int _poolCapacity;

	/**
	 * 获取或设置池中每一项的最大寿命（单位秒）。
	 * 
	 * 如果该值小于或者等于0，则代表允许池中的项无限制存在。
	 */
	private final int _maxLifespan;

	/**
	 * 停留时间（单位秒） 。
	 * 
	 * 如果项在池中超过停留时间，那么抛弃。
	 * 
	 * 如果项被使用，那么停留时间会被重置计算。
	 * 
	 * 如果该值小于或者等于0，则代表允许池中的项无限制存在。
	 */
	private final int _maxRemainTime;

	/**
	 * 获取或设置池中项在被移除或销毁之前，能使用的最大次数。
	 * 
	 * 如果该值小于或等于0，那么可以使用无限次。
	 */
	private final int _maxUses;

	private int _borrowedCount;

	/**
	 * 
	 * 获取已借出项的个数
	 * 
	 * @return
	 */
	public int getBorrowedCount() {
		synchronized (_syncRoot) {
			checkDisposed();
			return _borrowedCount;
		}
	}

	private int _waiterCount;

	/**
	 * 获取等待的消费者个数
	 * 
	 * @return
	 */
	public int getWaiterCount() {
		synchronized (_syncRoot) {
			checkDisposed();
			return _waiterCount;
		}
	}

	/// <summary>
	/// 借出的数量超过最大值
	/// </summary>
	/// <returns></returns>
	public Boolean isBorrowedOverstep() {
		synchronized (_syncRoot) {
			return this.isBorrowedOverstepImpl();
		}
	}

	private Boolean isBorrowedOverstepImpl() {
		return _loanCapacity > 0 && _borrowedCount >= _loanCapacity;
	}

	/**
	 * 初始化一个新的池实例
	 * 
	 * @param itemFactory   当池中没有可以重用的项时,用其创建一个项。
	 * @param itemFilter    当项离开或者回到池中时，会调用这个方法。如果项可以继续重用，返回true；
	 *                      如果项需要被抛弃并从池中被移除，那么返回false；当项是返回池中时，phase是
	 *                      PoolItemPhase.Returning，当项是离开池中时，phase
	 *                      是PoolItemPhase.Leaving,离开是指从池中移除，而不是借出去
	 * @param itemDestroyer 当项被消除时，会使用该对象进行额外的销毁操作,可为空
	 * @param config        池行为的配置
	 */
	public Pool(Func<T> itemFactory, Func2<T, PoolItemPhase, Boolean> itemFilter, Action1<T> itemDestroyer,
			PoolConfig config) {
		Objects.requireNonNull(itemFactory, "itemFactory");
		Objects.requireNonNull(config, "config");

		_itemFactory = itemFactory;
		_itemFilter = itemFilter;
		_itemDestroyer = itemDestroyer;

		_loanCapacity = config.getLoanCapacity();
		_poolCapacity = config.getPoolCapacity();
		_maxRemainTime = config.getMaxRemainTime();
		_maxLifespan = config.getMaxLifespan();

		_maxUses = config.getMaxUses();
		_fetchOrder = config.getFetchOrder();

		_container = createContainer();
	}

	public Pool(Func<T> itemFactory, Func2<T, PoolItemPhase, Boolean> itemFilter, PoolConfig config) {
		this(itemFactory, itemFilter, null, config);
	}

	public Pool(Func<T> itemFactory, PoolConfig config) {
		this(itemFactory, null, null, config);
	}

	private IPoolContainer<ResidentItem<T>> createContainer() {

		return _fetchOrder == PoolFetchOrder.Fifo ? new FifoContainer<ResidentItem<T>>()
				: new LifoContainer<ResidentItem<T>>();
	}

	private void checkDisposed() {
		if (_isDisposed)
			throw new IllegalStateException(strings("PoolDisposed", this.getClass().getName()));
	}

	public IPoolItem<T> borrow() throws Exception {
		ResidentItem<T> resident = null;
		ArrayList<ResidentItem<T>> expiredItems = null;
		try {

			int currentPoolVersion;

			synchronized (_syncRoot) {
				checkDisposed();

				currentPoolVersion = this._poolVersion; // 借出时，池的版本号

				while (this.isBorrowedOverstepImpl()) {
					// 当借出的数量超过指定数量时，等待
					++_waiterCount;
					_syncRoot.wait();// 释放当前线程对_syncRoot的锁，流放当前线程到等待队列,消费者的线程此时会被阻塞
					--_waiterCount;
				}

				while (_container.getCount() > 0) {
					resident = _container.take();
					// 如果项的寿命到期或者该项已被抛弃, 或超过了停留时间(停留时间在每次使用后会被刷新)
					if (isInvalid(resident) || !filter(resident, PoolItemPhase.Leaving)) {
						if (expiredItems == null)
							expiredItems = new ArrayList<ResidentItem<T>>();
						expiredItems.add(resident);// 那么将该项移到过期集合中
						resident = null;// 重置项指针指向null
						continue;
					}
					break;
				}

				++_borrowedCount;
			}

			try {
				// 如果在容器中没有找到可用的项，那么创建被封装的新项
				if (resident == null)
					resident = new ResidentItem<T>(this, _itemFactory.apply());

				var borrowedItem = resident.borrow(currentPoolVersion);

				return borrowedItem;
			} catch (Exception e) {
				decrementBorrowedCount();// 如果出错，则本次借出失败，减少一次借出的数量(因为之前++_borrowedCount)
				throw new PoolingException(strings("BorrowPoolItemFailed", this.getClass().getName()), e);
			}
		} catch (Exception e) {
			throw new PoolingException(strings("BorrowPoolItemFailed", this.getClass().getName()), e);
		} finally {
			if (expiredItems != null) {
				for (var item : expiredItems) {
					discardItem(item);
				}
			}
		}
	}

	/**
	 * 归还项
	 * 
	 * @param item
	 * @throws Exception
	 * @throws PoolingException
	 */
	public void back(IPoolItem<T> item) throws Exception {
		item.clear();
	}

	/**
	 * 使用池中的项
	 * 
	 * @param action
	 * @throws Exception
	 */
	public void using(Action1<T> action) throws Exception {
		IPoolItem<T> item = null;
		try {
			item = this.borrow();
			action.apply(item.getItem());
		} catch (Exception e) {
			if (item != null)
				item.setCorrupted();
			throw e;
		} finally {
			if (item != null)
				item.clear();
		}
	}

	public <R> R using(Func1<T, R> action) throws Exception {
		IPoolItem<T> item = null;
		try {
			item = this.borrow();
			return action.apply(item.getItem());
		} catch (Exception e) {
			if (item != null)
				item.setCorrupted();
			throw e;
		} finally {
			if (item != null)
				item.clear();
		}
	}

	/**
	 * 取池中当前项的数量
	 * 
	 * @return
	 */
	public int getCount() {

		synchronized (_syncRoot) {
			checkDisposed();
			return _container.getCount();
		}

	}

	/**
	 * 清理池
	 * 
	 * @throws PoolingException
	 */
	public void clear() throws PoolingException {
		IPoolContainer<ResidentItem<T>> oldContainer;

		synchronized (_syncRoot) {
			checkDisposed();

			_poolVersion++; // 更改池版本号，因此，借出的项在归还时，都会被清理

			oldContainer = _container;
			_container = createContainer();
		}

		clearPool(oldContainer);
	}

	/**
	 * 项是否无效
	 * 
	 * @param resident
	 * @return
	 */
	private boolean isInvalid(ResidentItem<T> resident) {
		return resident.isCorrupted() || isRemainTimeExpired(resident) || isLifespanExpired(resident)
				|| isOverstepUseCount(resident);
	}

	/**
	 * 是否超过了停留时间
	 * 
	 * @param residentItem
	 * @return
	 */
	private boolean isRemainTimeExpired(ResidentItem<T> residentItem) {

		return _maxRemainTime > 0
				&& Duration.between(residentItem.getLastUsedTime(), Instant.now()).getSeconds() >= _maxRemainTime;
	}

	/**
	 * 项的寿命是否到期
	 * 
	 * @param residentItem
	 * @return
	 */
	private boolean isLifespanExpired(ResidentItem<T> residentItem) {

		return _maxLifespan > 0
				&& Duration.between(residentItem.getCreateTime(), Instant.now()).getSeconds() >= _maxLifespan;
	}

	/**
	 * 超出使用次数
	 * 
	 * @param residentItem
	 * @return
	 */
	private boolean isOverstepUseCount(ResidentItem<T> residentItem) {
		return _maxUses > 0 && residentItem.getUseCount() >= _maxUses;
	}

	private boolean filter(ResidentItem<T> residentItem, PoolItemPhase phase) throws Exception {
		if (_itemFilter == null) {
			if (phase == PoolItemPhase.Returning && residentItem.isReusable()) {
				// 如果是可回收的，那么在回到池中时，清理下资源
				var reusable = (IReusable) residentItem.getItem();
				reusable.clear();
			}
			return true;
		}
		return _itemFilter.apply(residentItem.getItem(), phase);
	}

	/**
	 * 减少借出数量，同时将等待队列中的一个线程放行到就绪队列中
	 */
	private void decrementBorrowedCount() {
		synchronized (_syncRoot) {
			--_borrowedCount;
			if (_waiterCount > 0) {
				// 将等待队列中的一个线程放行到就绪队列
				_syncRoot.notify();
			}
		}
	}

	/**
	 * 向池中归还项
	 * 
	 * @param residentItem
	 * @throws PoolingException
	 */
	void back(ResidentItem<T> residentItem) throws Exception {
		decrementBorrowedCount();

		// 如果项被显示注明了需要抛弃
		// 或者项使用次数超过了限制
		// 或者项在池中的寿命超过了限制
		// 那么项被需要被抛弃
		boolean discard = isInvalid(residentItem);

		if (!discard) // 如果项没有被抛弃，那么调用过滤方法，进一步判断，是否被抛弃
			discard = !filter(residentItem, PoolItemPhase.Returning);

		if (discard) {
			discardItem(residentItem);
			return;
		}

		boolean returned = false;
		synchronized (_syncRoot) {
			if (!_isDisposed && residentItem.getPoolVersionWhenBorrowed() == this._poolVersion
					&& (_poolCapacity <= 0 || _container.getCount() < _poolCapacity)) {
				// 如果池没有被释放
				// 并且项的池版本等于当前池的版本
				// 并且池中的项数量没有达到限定值
				// 那么将项放入容器中
				_container.put(residentItem);
				returned = true;
			}
		}
		if (!returned) // 如果没有返回，那么移除项
			discardItem(residentItem);
	}

	/**
	 * 抛弃并释放池中的项
	 * 
	 * @param residentItem
	 * @throws PoolingException
	 */
	private void discardItem(ResidentItem<T> residentItem) throws PoolingException {
		try {
			if (_itemDestroyer != null)
				_itemDestroyer.apply(residentItem.getItem());

			var disposableObject = as(residentItem.getItem(), AutoCloseable.class);
			if (disposableObject != null)
				disposableObject.close();
		} catch (Exception e) {
			throw new PoolingException(strings("DisposePoolItemFailed", this.getClass().getName()), e);
		}
	}

	/**
	 * 清理池中所有项，并且标示池已被释放，不能再使用
	 */
	@Override
	public void close() throws Exception {
		synchronized (_syncRoot) {
			_isDisposed = true;
			clearPool(_container);
		}

	}

	/**
	 * 实现真正清理池的动作
	 * 
	 * @param container
	 * @throws PoolingException
	 */
	private void clearPool(IPoolContainer<ResidentItem<T>> container) throws PoolingException {
		while (container.getCount() > 0) {
			var item = container.take();
			discardItem(item);
		}
	}

}
