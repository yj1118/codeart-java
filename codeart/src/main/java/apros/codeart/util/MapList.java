package apros.codeart.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Iterables;

public class MapList<TKey, TValue> {

	private HashMap<TKey, ArrayList<TValue>> _data;
	private boolean _disposeKey;

	private BiFunction<TValue, TValue, Boolean> _equals;

	/**
	 * 当key对应的Value集合的成员数量为0时，是否销毁key
	 * 
	 * 如果销毁key，在下次添加同样的key时，会重新创建list
	 * 
	 * 设置为false，可以节约创建list的开销，但是如果key长期不用，会浪费内存
	 * 
	 * @param disposeKey
	 */
	public MapList(boolean disposeKey) {
		this(16, disposeKey, null);
	}

	public MapList(boolean disposeKey, BiFunction<TValue, TValue, Boolean> equals) {
		this(16, disposeKey, equals);
	}

	public MapList(int capacity, boolean disposeKey, BiFunction<TValue, TValue, Boolean> equals) {
		_data = new HashMap<TKey, ArrayList<TValue>>(capacity);
		_disposeKey = disposeKey;
		_equals = equals;
	}

	private ArrayList<TValue> obtain(TKey key) {
		ArrayList<TValue> list = _data.get(key);

		if (list == null) {
			list = new ArrayList<TValue>();
			_data.put(key, list);
		}
		return list;
	}

	public void put(TKey key, TValue value) {
		ArrayList<TValue> list = obtain(key);
		list.add(value);
	}

	/**
	 * 如果没有重复项，那么添加value，返回true,否则返回false,不添加
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean tryPut(TKey key, TValue value) {
		ArrayList<TValue> list = obtain(key);
		if (contains(list, value))
			return false;
		list.add(value);
		return true;
	}

	private boolean contains(ArrayList<TValue> list, TValue value) {
		if (list == null)
			return false;
		if (_equals == null) {
			return list.contains(value);
		} else {
			return ListUtil.contains(list, value, _equals);
		}
	}

	/**
	 * 
	 * 集合 values 中的重复项不会被添加，非重复项被添加，如果没有任何重复项，返回true,否则返回false
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public boolean tryPut(TKey key, Iterable<TValue> values) {
		ArrayList<TValue> list = obtain(key);

		boolean contains = false;
		for (var value : values) {
			if (contains(list, value)) {
				contains = true;
				continue;
			}
			list.add(value);
		}
		return contains;
	}

	public boolean remove(TKey key) {
		return _data.remove(key) != null;
	}

	public Iterable<TValue> get(TKey key) {
		return _data.get(key);
	}

	public Iterable<TKey> getKeys() {
		return _data.keySet();
	}

	public boolean containsKey(TKey key) {
		return _data.containsKey(key);
	}

	/**
	 * 
	 * 判断是否含有{@code key}，且{@code key}下含有值 {@code value}
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean containsValue(TKey key, TValue value) {
		var list = _data.get(key);
		return contains(list, value);
	}

	private void tryDisposeKey(TKey key, ArrayList<TValue> list) {
		if (list.size() == 0 && _disposeKey)
			_data.remove(key); // 集合为0了，从data中移除
	}

	public boolean removeValue(TKey key, TValue value) {
		if (_equals != null) {
			return removeValue(key, (item) -> {
				return _equals.apply(item, value);
			});
		}

		return removeValue(key, (item) -> {
			return item.equals(value);
		});
	}

	/**
	 * 
	 * 删除满足条件的第一项
	 * 
	 * @param key
	 * @param predicate
	 * @return
	 */
	public boolean removeValue(TKey key, Function<TValue, Boolean> predicate) {
		var list = _data.get(key);
		if (list == null)
			return false;

		boolean removed = ListUtil.removeFirst(list, (item) -> {
			return predicate.apply(item);
		}) != null;

		if (removed) {
			tryDisposeKey(key, list); // 集合为0了，从data中移除
			return true;
		}
		return false;
	}

	/// <summary>
	/// 删除所有满足条件的value
	/// </summary>
	/// <param name="key"></param>
	/// <param name="find"></param>
	/// <returns></returns>
	public int removeValues(TKey key, Function<TValue, Boolean> predicate) {
		var list = _data.get(key);
		if (list == null)
			return 0;

		var beforeCount = list.size();

		Iterables.removeIf(list, (item) -> {
			return predicate.apply(item);
		});

		var count = beforeCount - list.size();

		if (count > 0) {
			tryDisposeKey(key, list);
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	public int removeValues(Function<TValue, Boolean> predicate) {

		if (_disposeKey) {
			var keys = _data.keySet().toArray();

			int count = 0;
			for (var key : keys) {
				count += removeValues((TKey) key, predicate);
			}
			return count;
		} else {
			Iterable<TKey> keys = _data.keySet();

			int count = 0;
			for (var key : keys) {
				count += removeValues(key, predicate);
			}
			return count;
		}
	}

	public Iterable<TValue> getValues(TKey key) {
		return _data.get(key);
	}

	public TValue getValue(TKey key, Function<TValue, Boolean> predicate) {
		var list = _data.get(key);
		if (list == null)
			return null;

		return ListUtil.find(list, predicate);
	}

	/**
	 * 针对所有的values找值
	 * 
	 * @param predicate
	 * @return
	 */
	public TValue getValue(Function<TValue, Boolean> predicate) {
		for (var list : _data.values()) {
			var value = ListUtil.find(list, predicate);
			if (value != null)
				return value;
		}
		return null;
	}

	public Iterable<TValue> getValues(TKey key, Function<TValue, Boolean> predicate) {
		var list = _data.get(key);
		if (list == null)
			return ListUtil.<TValue>empty();

		return ListUtil.filter(list, predicate);
	}

	public Iterable<TValue> getValues(Function<TValue, Boolean> predicate) {

		ArrayList<TValue> result = null;
		for (var p : _data.entrySet()) {
			var list = p.getValue();
			for (var value : list) {
				if (predicate.apply(value)) {
					if (result == null)
						result = new ArrayList<TValue>();
					result.add(value);
				}
			}
		}

		return result == null ? ListUtil.<TValue>empty() : result;
	}

	public Iterable<TValue> getValues() {
		return getValues((value) -> {
			return true;
		});
	}

	public void clear() {
		_data.clear();
	}

	public void each(Consumer<TValue> action) {
		for (var p : _data.entrySet()) {
			var list = p.getValue();
			for (var value : list) {
				action.accept(value);
			}
		}
	}

	public void each(BiConsumer<TKey, Iterable<TValue>> action) {
		for (var p : _data.entrySet()) {
			var key = p.getKey();
			var list = p.getValue();
			action.accept(key, list);
		}
	}
}
