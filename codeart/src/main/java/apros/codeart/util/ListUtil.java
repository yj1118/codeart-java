package apros.codeart.util;

import static apros.codeart.runtime.Util.propagate;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import apros.codeart.bytecode.ClassGenerator;

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
		ArrayList<T> items = null;
		for (T item : source) {
			if (predicate.apply(item)) {
				if (items == null)
					items = new ArrayList<T>();
				items.add(item);
			}
		}
		return items == null ? ListUtil.<T>empty() : items;
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
			if (predicate.apply(item)) {
				if (items == null)
					items = new ArrayList<T>();
				items.add(item);
			}
		}
		return items == null ? ListUtil.<T>empty() : items;
	}

	public static boolean contains(Iterable<Integer> source, Integer target) {
		for (Integer item : source) {
			if (item == target)
				return true;
		}
		return false;
	}

	public static <T> boolean contains(Iterable<T> source, Function<T, Boolean> predicate) {
		return find(source, predicate) != null;
	}

	public static <T> boolean contains(Iterable<T> source, T value, BiFunction<T, T, Boolean> equals) {
		for (T item : source) {
			if (equals.apply(item, value))
				return true;
		}
		return false;
	}

	public static <T> boolean contains(T[] source, Function<T, Boolean> predicate) {
		return find(source, predicate) != null;
	}

	public static <T> T first(Iterable<T> source) {
		for (T item : source) {
			return item;
		}
		return null;
	}

	public static <T, R> ArrayList<R> map(Iterable<T> source, Function<T, R> selector) {
		ArrayList<R> list = new ArrayList<R>(Iterables.size(source));
		for (T item : source) {
			list.add(selector.apply(item));
		}
		return list;
	}

	public static <T, R> ArrayList<R> map(T[] source, Function<T, R> selector) {
		ArrayList<R> list = new ArrayList<R>(source.length);
		for (T item : source) {
			list.add(selector.apply(item));
		}
		return list;
	}

	public static <T> ArrayList<T> asList(T[] source) {
		ArrayList<T> list = new ArrayList<T>(source.length);
		for (T item : source) {
			list.add(item);
		}
		return list;
	}

	public static <T> ArrayList<T> asList(Iterable<T> source) {
		ArrayList<T> list = new ArrayList<T>(Iterables.size(source));
		for (T item : source) {
			list.add(item);
		}
		return list;
	}

	public static <T> List<T> asReadonly(Iterable<T> source) {
		return ImmutableList.copyOf(source);
	}

	public static <T, R> ArrayList<R> mapMany(T[] source, Function<T, Iterable<R>> selector) {
		ArrayList<R> list = new ArrayList<R>(source.length);
		for (T item : source) {
			var items = selector.apply(item);
			addRange(list, items);
		}
		return list;
	}

	public static <T> T removeFirst(Iterable<T> source, Predicate<T> predicate) {
		var iterator = source.iterator();
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (predicate.test(item)) {
				iterator.remove(); // 使用迭代器的 remove() 方法安全删除当前元素
				return item;
			}
		}
		return null;
	}

	public static <T> void remove(Iterable<T> source, Predicate<T> predicate) {
		var iterator = source.iterator();
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (predicate.test(item)) {
				iterator.remove();
			}
		}
	}

	public static <T> void addRange(AbstractList<T> source, Iterable<T> collection) {
		addRange(source, collection, false);
	}

	public static <T> void addRange(AbstractList<T> source, Iterable<T> collection, boolean distinct) {
		if (collection == null)
			return;
		if (distinct) {
			for (T item : collection) {
				if (!source.contains(item))
					source.add(item);
			}
		} else {
			for (T item : collection) {
				source.add(item);
			}
		}

	}

	public static <T> void addRange(AbstractList<T> source, T[] collection) {
		addRange(source, collection, false);
	}

	public static <T> void addRange(AbstractList<T> source, T[] collection, boolean distinct) {
		if (collection == null)
			return;
		if (distinct) {
			for (T item : collection) {
				if (!source.contains(item))
					source.add(item);
			}
		} else {
			for (T item : collection) {
				source.add(item);
			}
		}
	}

	public static <T> LinkedList<T> reverse(LinkedList<T> source) {
		LinkedList<T> temp = new LinkedList<>(source);
		Collections.reverse(temp);
		return temp;
	}

	@SuppressWarnings("unused")
	public static boolean exists(Iterable<?> e) {
		if (e == null)
			return false;
		for (var t : e)
			return true;
		return false;
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

	/// <summary>
	/// 将集合<paramref name="source"/>转变成为<paramref name="target"/>，需要增加哪些元素和需要删除哪些元素
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="source"></param>
	/// <param name="target"></param>
	/// <returns></returns>
	public static <T> TransformResult<T, T> transform(Iterable<T> source, Iterable<T> target) {
		return transform(source, target, (s, t) -> {
			return s.equals(t);
		});
	}

	public static record TransformResult<TT, ST>(Iterable<TT> adds, Iterable<ST> removes, Iterable<TT> updates) {
	}

	/// <summary>
	/// 将集合<paramref name="source"/>转变成为<paramref name="target"/>，需要增加哪些元素和需要删除哪些元素
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="source"></param>
	/// <param name="target"></param>
	/// <param name="equals"></param>
	/// <returns></returns>
	public static <TT, ST> TransformResult<TT, ST> transform(Iterable<ST> source, Iterable<TT> target,
			BiFunction<ST, TT, Boolean> equals) {
		ArrayList<ST> souceCopy = asList(source);
		ArrayList<TT> targetCopy = asList(target);

		if (Iterables.size(source) == 0)
			return new TransformResult<TT, ST>(targetCopy, ListUtil.empty(), ListUtil.empty());

		if (Iterables.size(target) == 0)
			return new TransformResult<TT, ST>(ListUtil.empty(), souceCopy, ListUtil.empty());

		List<TT> sames = new ArrayList<TT>(); // 需要保留的

		// 有相同的
		for (var item : source) {
			var same = ListUtil.find(target, (t) -> equals.apply(item, t));

			if (same != null) // 找到相同的保留
			{
				sames.add(same);
			}

		}

		for (var same : sames) {
			ListUtil.removeFirst(souceCopy, (item) -> equals.apply(item, same));
			targetCopy.remove(same);
		}

		return new TransformResult<TT, ST>(targetCopy, souceCopy, sames);
	}

	private static final Object Empty;

	private static Object createEmpty() {
		try (var cg = ClassGenerator.define()) {

			try (var mg = cg.defineMethodPublicStatic("getList", List.class)) {
				mg.newList().asReadonlyList();
			}

			var cls = cg.toClass();

			var method = cls.getDeclaredMethod("getList");
			return method.invoke(null);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private static final int[] EmptyInts;

	private static final byte[] EmptyBytes;

	private static final Object[] EmptyObjects;

	static {
		Empty = createEmpty();
		EmptyInts = new int[] {};
		EmptyBytes = new byte[] {};
		EmptyObjects = new Object[] {};
	}

	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> empty() {
		return (Iterable<T>) Empty;
	}

	public static int[] emptyInts() {
		return EmptyInts;
	}

	public static byte[] emptyByts() {
		return EmptyBytes;
	}

	public static Object[] emptyObjects() {
		return EmptyObjects;
	}

}
