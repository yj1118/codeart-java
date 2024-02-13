package apros.codeart.pooling;

import static apros.codeart.i18n.Language.strings;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Pool<T> implements AutoCloseable {

	/// <summary>
	/// 同步对象
	/// </summary>
	private final Object _syncRoot = new Object();
	private final Supplier<T> _itemFactory;
	private final BiFunction<T, PoolItemPhase, Boolean> _itemFilter;
	private final Consumer<T> _itemDestroyer; // 毁灭者，当项被消除时，会使用该对象进行额外的销毁操作
	private IPoolContainer<ResidentItem<T>> _container;
	private boolean _isDisposed;

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
			if (_isDisposed)
				throw new IllegalStateException(strings("PoolDisposed"));
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
			if (_isDisposed)
				throw new IllegalStateException(strings("PoolDisposed"));
			return _waiterCount;
		}
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
	public Pool(Supplier<T> itemFactory, BiFunction<T, PoolItemPhase, Boolean> itemFilter, Consumer<T> itemDestroyer,
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

		_container = createContainer(config.getPoolCapacity() > 0 ? config.getPoolCapacity() : 10);
	}

	private IPoolContainer<ResidentItem<T>> createContainer(int capacity) {
		if (_fetchOrder == PoolFetchOrder.Fifo) {
			return new FifoContainer<ResidentItem>(capacity);
		} else if (_fetchOrder == PoolFetchOrder.Lifo) {
			return new LifoContainer<ResidentItem>(capacity);
		} else {
			throw new NotSupportedException(string.Format(Strings.NotSupportedPoolFetchOrder, _fetchOrder));
		}
	}

	/// <summary>
	/// 向池中归还项
	/// </summary>
	/// <param name="residentItem"></param>
	void back(ResidentItem<T> residentItem) {
			DecrementBorrowedCount();

	     //如果项被显示注明了需要抛弃
	     //    或者项使用次数超过了限制
	     //    或者项在池中的寿命超过了限制
	     //那么项被需要被抛弃
	     bool discard = IsExpired(residentItem);

			if (!discard) //如果项没有被抛弃，那么调用过滤方法，进一步判断，是否被抛弃
	         discard = !_filter(residentItem, PoolItemPhase.Returning);

			if (discard)
			{
				DiscardItem(residentItem);
				return;
			}

			bool returned = false;
			lock (_syncRoot)
			{
				if (!_isDisposed
					&& residentItem.PoolVersionWhenBorrowed == this._poolVersion
					&& (_poolCapacity <= 0 || _container.Count < _poolCapacity))
				{
	             //如果池没有被释放
	             //    并且项的池版本等于当前池的版本
	             //    并且池中的项数量没有达到限定值
	             //那么将项放入容器中
					_container.Put(residentItem);
					returned = true;
				}
			}
			if (!returned) //如果没有返回，那么移除项
	         DiscardItem(residentItem);
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub

	}

}
