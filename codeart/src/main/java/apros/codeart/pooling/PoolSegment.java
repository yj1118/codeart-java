package apros.codeart.pooling;

import static apros.codeart.i18n.Language.strings;
import static apros.codeart.runtime.TypeUtil.as;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PoolSegment<T> implements IPoolSegment {

	private final Supplier<T> _itemFactory;
	/**
	 * 当项被返回到池中，会被回收者回收一次
	 */
	private final Consumer<T> _itemRecycler;
	private final Consumer<T> _itemDestroyer; // 当项被消除时，会使用该对象进行额外的销毁操作
	private ResidentItem[] _container;
	private boolean _isDisposed;
	private int _poolVersion;

	/**
	 * 池的容量
	 */
	private final int _capacity;

	private AtomicInteger _pointer = new AtomicInteger(-1);

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
	public PoolSegment(Supplier<T> itemFactory, Consumer<T> itemRecycler, Consumer<T> itemDestroyer,
			PoolConfig config) {
		Objects.requireNonNull(itemFactory, "itemFactory");
		Objects.requireNonNull(config, "config");

		_itemFactory = itemFactory;
		_itemRecycler = itemRecycler;
		_itemDestroyer = itemDestroyer;

		_capacity = config.getMinCapacity();

		_container = createContainer();
	}

	public PoolSegment(Supplier<T> itemFactory, Consumer<T> itemRecycler, PoolConfig config) {
		this(itemFactory, itemRecycler, null, config);
	}

	public PoolSegment(Supplier<T> itemFactory, PoolConfig config) {
		this(itemFactory, null, null, config);
	}

	private ResidentItem[] createContainer() {

		var items = new ResidentItem[_capacity];

		for (var i = 0; i < items.length; i++) {
			items[i] = new ResidentItem(this, _itemFactory.get());
		}

		return items;
	}

	private void checkDisposed() {
		if (_isDisposed)
			throw new IllegalStateException(strings("codeart", "PoolDisposed", this.getClass().getName()));
	}

	private int getIndex() {
		return _pointer.updateAndGet(current -> (current + 1) % _capacity);
	}

	public IPoolItem borrow() {

		var index = getIndex();

		var item = _container[index];

		if (item.tryClaim())
			return item;

		// 由于循环获得的项失败了，表示整个池已经满了，所以创建临时项给外界用
		return new TempItem(this, _itemFactory.get());
	}

	/**
	 * 使用池中的项
	 * 
	 * @param action
	 * @throws Exception
	 */
	public void using(Consumer<T> action) {
		try (IPoolItem item = this.borrow()) {
			action.accept(item.getItem());
		}
	}

	public <R> R using(Function<T, R> action) {
		try (IPoolItem item = this.borrow()) {
			return action.apply(item.getItem());
		}
	}

	/**
	 * 清理池
	 * 
	 */
	public void clear() {
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
	 * 向池中归还项
	 * 
	 * @param item
	 */
	public void clear(IPoolItem item) {

		// 实际上只用执行回收处理器即可
		if (_itemRecycler != null) {
			_itemRecycler.accept(item.getItem());
		}
	}

	/**
	 * 抛弃并释放池中的项
	 * 
	 * @param residentItem
	 * @throws PoolingException
	 */
	private void discardItem(ResidentItem residentItem) throws PoolingException {
		try {
			if (_itemDestroyer != null)
				_itemDestroyer.accept(residentItem.getItem());

			var disposableObject = as(residentItem.getItem(), AutoCloseable.class);
			if (disposableObject != null)
				disposableObject.close();
		} catch (Exception e) {
			throw new PoolingException(strings("codeart", "DisposePoolItemFailed", this.getClass().getName()), e);
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
