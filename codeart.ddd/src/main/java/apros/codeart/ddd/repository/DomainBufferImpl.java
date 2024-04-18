package apros.codeart.ddd.repository;

import java.util.ArrayList;
import java.util.function.Supplier;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.UniqueKeyCalculator;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;

class DomainBufferImpl {

	private ArrayList<IAggregateRoot> _items;

	public DomainBufferImpl() {
		_items = new ArrayList<IAggregateRoot>();
	}

	public void clear() {
		_items.clear();
	}

	public void add(IAggregateRoot obj) {

		if (ListUtil.contains(_items, (t) -> {
			return obj == t || t.uniqueKey().equals(obj.uniqueKey());
		}))
			return;
		_items.add(obj);
	}

	public void remove(Class<?> objectType, Object id) {
		var uniqueKey = getUniqueKey(objectType, id);
		remove(uniqueKey);
	}

	private void remove(String uniqueKey) {
		var entry = ListUtil.removeFirst(_items, (t) -> {
			return t.uniqueKey().equals(uniqueKey);
		});

		if (entry != null) {
			// 缓冲区的数据移除后，也要主动将数据代理给清空
			// 因为数据代理可能包含一些线程公共资源，这些资源必须清理，不然下次加载数据，又会使用这些公共资源
			// 数据代理中AppSession就是典型的例子（注意，该注释已经过时了，目前保留只是为了查阅历史用）
			var obj = TypeUtil.as(entry, DomainObject.class);
			obj.dataProxy().clear();
		}
	}

	private static String getUniqueKey(Class<?> objectType, Object id) {
		return UniqueKeyCalculator.getUniqueKey(objectType, id);
	}

//	 #region 在缓冲池中加载或创建对象

	public IAggregateRoot obtain(Class<?> objectType, Object id, int dataVersion, Supplier<IAggregateRoot> load) {
		var uniqueKey = getUniqueKey(objectType, id);
		return obtainImpl(uniqueKey, dataVersion, load);
	}

	/// <summary>
	/// 从缓存区中创建或者获取数据
	/// </summary>
	/// <param name="tip"></param>
	/// <param name="getCacheKey"></param>
	/// <param name="dataVersion"></param>
	/// <param name="load"></param>
	/// <returns></returns>
	private IAggregateRoot obtainImpl(String uniqueKey, int dataVersion, Supplier<IAggregateRoot> load) {
		var result = ListUtil.find(_items, (t) -> t.uniqueKey().equals(uniqueKey));

		if (result != null) {
			if (result.dataVersion() == dataVersion)
				return result;
		}

		// 更新缓冲区
		var root = load.get();

		if (result != null) {
			// 删除老数据
			ListUtil.removeFirst(_items, (t) -> t.uniqueKey().equals(uniqueKey));
		}

		_items.add(root);
		return root;
	}

//	 #endregion

	/**
	 * 获取数据上下文中存放的缓冲对象
	 * 
	 * @return
	 */
	Iterable<IAggregateRoot> items() {
		return _items;
	}
}
