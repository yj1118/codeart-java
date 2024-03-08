package com.apros.codeart.util;

import java.util.HashMap;
import java.util.function.Function;

/**
 * 懒惰索引器
 */
public class LazyIndexer {
	private LazyIndexer() {
	}

	/**
	 * 创建懒惰索引器
	 * 
	 * 只有当key第一次出现时才会使用你提供的方法创建value
	 * 
	 * 返回的方法是线程安全的
	 * 
	 * @param <TKey>
	 * @param <TValue>
	 * @param valueFactory 为key创建value的方法
	 * @param filter       过滤项，根据value的值确定是否抛弃，返回true表示不抛弃，返回false表示抛弃
	 * @return
	 */
	public static <TKey, TValue> Function<TKey, TValue> init(Function<TKey, TValue> valueFactory,
			Function<TValue, Boolean> filter) {
		if (valueFactory == null)
			throw new ArgumentNullException("valueFactory");
		var map = new HashMap<TKey, TValue>();
		return key -> {
			TValue result = map.get(key);
			if (result != null)
				return result;
			synchronized (map) {
				result = map.get(key);
				if (result != null)
					return result;
				var newValue = valueFactory.apply(key);
				if (filter == null || filter.apply(newValue)) {
					// 为了防止valueFactory内进行了缓存，这里再次判断一下
					result = map.get(key);
					if (result != null)
						return result;
					map.put(key, newValue);
				}
				return newValue;
			}
		};
	}

	public static <TKey, TValue> Function<TKey, TValue> init(Function<TKey, TValue> valueFactory) {
		return init(valueFactory, null);
	}
}
