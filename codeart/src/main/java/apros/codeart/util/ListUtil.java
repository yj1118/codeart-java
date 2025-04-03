package apros.codeart.util;

import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import apros.codeart.bytecode.ClassGenerator;

public final class ListUtil {
    private ListUtil() {
    }

    ;

    public static <T> T find(Iterable<T> source, Function<T, Boolean> predicate) {
        if (source == null) return null;
        for (T item : source) {
            if (predicate.apply(item))
                return item;
        }
        return null;
    }

    public static <T> T find(Iterable<T> source, Function<T, Boolean> predicate, T defaultValue) {
        return Objects.requireNonNullElse(find(source, predicate), defaultValue);
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

    public static boolean contains(Iterable<Integer> source, Integer target) {
        for (Integer item : source) {
            if (Objects.equals(item, target))
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

    public static boolean containsIgnoreCase(Iterable<String> list, String target) {
        for (String element : list) {
            if (element.equalsIgnoreCase(target)) {
                return true;
            }
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

    public static ArrayList<Long> asList(long[] source) {
        Long[] longObjects = Arrays.stream(source)
                .boxed() // box the primitive long to Long
                .toArray(Long[]::new);

        return asList(longObjects);
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

    public static Iterable<?> asList(Object value) {
        Iterable<?> values;
        if (value instanceof Iterable) {
            values = (Iterable<?>) value;
        } else if (value != null && value.getClass().isArray()) {
            // 基本类型数组处理
            if (value instanceof long[]) {
                values = Arrays.stream((long[]) value).boxed().toList();
            } else if (value instanceof int[]) {
                values = Arrays.stream((int[]) value).boxed().toList();
            } else if (value instanceof double[]) {
                values = Arrays.stream((double[]) value).boxed().toList();
            } else if (value instanceof boolean[] booleanArray) {
                List<Boolean> booleanList = new ArrayList<>();
                for (boolean b : booleanArray) {
                    booleanList.add(b); // 自动装箱为 Boolean
                }
                values = booleanList;
            } else if (value instanceof char[] charArray) {
                List<Character> charList = new ArrayList<>();
                for (char c : charArray) {
                    charList.add(c); // 自动装箱为 Character
                }
                values = charList;
            } else if (value instanceof byte[] byteArray) {
                List<Byte> byteList = new ArrayList<>();
                for (byte b : byteArray) {
                    byteList.add(b); // 自动装箱为 Boolean
                }
                values = byteList;
            } else if (value instanceof short[] shortArray) {
                List<Short> shortList = new ArrayList<>();
                for (Short s : shortArray) {
                    shortList.add(s); // 自动装箱为 Boolean
                }
                values = shortList;
            } else if (value instanceof float[] floatArray) {
                List<Float> floatList = new ArrayList<>();
                for (float f : floatArray) {
                    floatList.add(f); // 自动装箱为 Float
                }
                values = floatList;
            } else {
                // 引用类型数组
                values = Arrays.asList((Object[]) value);
            }
        } else {
            values = null;
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] asArray(Iterable<T> source, Class<T> type) {
        T[] array = (T[]) Array.newInstance(type, Iterables.size(source));

        var index = 0;
        for (var s : source) {
            array[index] = s;
            index++;
        }

        return array;
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
     * 是否有重复项
     *
     * @param list
     * @return
     */
    public static <T> boolean hasDuplicates(Iterable<T> list, Function<T, Object> selector) {
        Set<Object> set = new HashSet<>();
        for (T item : list) {
            var value = selector.apply(item);
            if (!set.add(value)) {
                return true; // 有重复
            }
        }
        return false; // 无重复
    }

    /**
     * 让集合高效的设置元素，防止2次遍历
     *
     * @param <T>
     * @param source
     * @param predicate
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

    public static <T, E> List<E> flatMap(Iterable<T> list, Function<T, Iterable<E>> getCollection) {
        var r = new ArrayList<E>();
        for (T item : list) {
            var children = getCollection.apply(item);
            addRange(r, children);
        }
        return r;
    }

    /**
     * 将集合 source 转变成为 target，需要增加哪些元素和需要删除哪些元素
     *
     * @param source
     * @param target
     * @param <T>
     * @return
     */
    public static <T> TransformResult<T, T> transform(Iterable<T> source, Iterable<T> target) {
        return transform(source, target, (s, t) -> {
            return s.equals(t);
        });
    }

    public static record TransformResult<TT, ST>(Iterable<TT> adds, Iterable<ST> removes, Iterable<TT> updates) {

        public boolean isEmpty() {
            return Iterables.isEmpty(adds) && Iterables.isEmpty(removes) && Iterables.isEmpty(updates);
        }

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
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    private static final int[] EmptyInts;

    private static final long[] EmptyLongs;

    private static final byte[] EmptyBytes;


    private static final UUID[] EmptyGUIDs;


    private static final Object[] EmptyObjects;

    static {
        Empty = createEmpty();
        EmptyInts = new int[]{};
        EmptyLongs = new long[]{};
        EmptyBytes = new byte[]{};
        EmptyGUIDs = new UUID[]{};
        EmptyObjects = new Object[]{};
    }

    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> empty() {
        return (Iterable<T>) Empty;
    }

    public static long[] emptyLongs() {
        return EmptyLongs;
    }

    public static int[] emptyInts() {
        return EmptyInts;
    }

    public static byte[] emptyBytes() {
        return EmptyBytes;
    }

    public static UUID[] emptyGUIDs() {
        return EmptyGUIDs;
    }


    public static Object[] emptyObjects() {
        return EmptyObjects;
    }

}
