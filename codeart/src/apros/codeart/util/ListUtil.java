package apros.codeart.util;

import java.util.ArrayList;
import java.util.function.Function;

public final class ListUtil {
	private ListUtil() {
	};

	public static <T> T findExable(Iterable<T> source, Func1<T, Boolean> predicate) throws Exception {
		for (T item : source) {
			if (predicate.apply(item))
				return item;
		}
		return null;
	}

	public static <T> T find(Iterable<T> source, Function<T, Boolean> predicate) {
		for (T item : source) {
			if (predicate.apply(item))
				return item;
		}
		return null;
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

	/**
	 * 删除满足条件的第一个项
	 * 
	 * @param <T>
	 * @param source
	 * @param predicate
	 * @return
	 * @throws Exception
	 */
	public static <T> T removeExable(Iterable<T> source, Func1<T, Boolean> predicate) throws Exception {
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

	public static <T> void addRange(ArrayList<T> source, Iterable<T> collection) {
		for (T item : collection) {
			source.add(item);
		}
	}

}
