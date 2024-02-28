package com.apros.codeart.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Iterables;

public final class ListUtil {
	private ListUtil() {
	};

	public static <T> T find(Iterable<T> source, Function<T, Boolean> predicate) {
		for (T item : source) {
			if (predicate.apply(item))
				return item;
		}
		return null;
	}

	public static <T> Iterable<T> filter(Iterable<T> source, Function<T, Boolean> predicate) {
		ArrayList<T> items = new ArrayList<T>();
		for (T item : source) {
			if (predicate.apply(item))
				items.add(item);
		}
		return items;
	}

	public static <T> T find(T[] source, Function<T, Boolean> predicate) {
		for (T item : source) {
			if (predicate.apply(item))
				return item;
		}
		return null;
	}

	public static <T> Iterable<T> filter(T[] source, Function<T, Boolean> predicate) {
		ArrayList<T> items = new ArrayList<T>();
		for (T item : source) {
			if (predicate.apply(item))
				items.add(item);
		}
		return items;
	}

	public static boolean contains(Iterable<Integer> source, Integer target) {
		for (Integer item : source) {
			if (item == target)
				return true;
		}
		return false;
	}

	public static <T> T first(Iterable<T> source) {
		for (T item : source) {
			return item;
		}
		return null;
	}

	public static <T, R> Iterable<R> map(Iterable<T> source, Function<T, R> selector) {
		ArrayList<R> list = new ArrayList<R>(Iterables.size(source));
		for (T item : source) {
			list.add(selector.apply(item));
		}
		return list;
	}

	/**
	 * 删除满足条件的第一个项
	 * 
	 * @param <T>
	 * @param source
	 * @param predicate
	 * @return
	 * @throws Exception
	 */
	public static <T> T removeExable(Iterable<T> source, Function<T, Boolean> predicate) {
		var iterator = source.iterator();
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (predicate.apply(item)) {
				iterator.remove(); // 使用迭代器的 remove() 方法安全删除当前元素
				return item;
			}
		}
		return null;
	}

	public static <T> T remove(Iterable<T> source, Function<T, Boolean> predicate) {
		var iterator = source.iterator();
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (predicate.apply(item)) {
				iterator.remove(); // 使用迭代器的 remove() 方法安全删除当前元素
				return item;
			}
		}
		return null;
	}

	public static <T> void addRange(AbstractList<T> source, Iterable<T> collection) {
		for (T item : collection) {
			source.add(item);
		}
	}

	/**
	 * 让集合高效的设置元素，防止2次遍历
	 * 
	 * @param <T>
	 * @param source
	 * @param predicate
	 * @param target
	 */
	public static <T> void set(AbstractList<T> source, Function<T, Boolean> predicate, Supplier<T> getTarget) {
		var iterator = source.listIterator();
		while (iterator.hasNext()) {
			if (predicate.apply(iterator.next())) {
				iterator.set(getTarget.get());
				break; // 找到目标元素后立即退出循环
			}
		}
	}

}
