package apros.codeart.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Function;

/**
 * 懒惰索引器
 */
public class LazyIndexer {
	private LazyIndexer() {
	}

//	/// <summary>
//	/// <para></para>
//	/// <para>只有当key第一次出现时才会使用你提供的方法创建value</para>
//	/// <para>该机制是线程安全的</para>
//	/// </summary>
//	/// <typeparam name="TKey"></typeparam>
//	/// <typeparam name="TValue"></typeparam>
//	/// <param name="valueFactory"></param>
//	/// <returns></returns>
//
//	/**
//	 * 给予方法懒惰的能力
//	 * @param <TKey>
//	 * @param <TValue>
//	 * @param valueFactory
//	 * @return
//	 */
//	public static<TKey,TValue> Function<TKey, TValue> init(Function<TKey, TValue> valueFactory)
//	{
//            return init.<TKey, TValue>(valueFactory, (value) => { return true; }, EqualityComparer<TKey>.Default);
//    }
//
//	public static Func<TKey, TValue> Init<TKey,TValue>(
//	Func<TKey, TValue> valueFactory, IEqualityComparer<TKey>comparer)
//	{
//            return Init<TKey, TValue>(valueFactory, (value) => { return true; }, comparer);
//        }
//
//	public static Func<TKey, TValue> Init<TKey,TValue>(
//	Func<TKey, TValue> valueFactory, Func<TValue,bool>filter)
//	{
//            return Init<TKey, TValue>(valueFactory, filter, EqualityComparer<TKey>.Default);
//        }

	/// <summary>
	/// <para>创建懒惰索引器</para>
	/// <para>只有当key第一次出现时才会使用你提供的方法创建value</para>
	/// <para>你还可以提供一个IEqualityComparer{TKey}的实现，用于对key进行排序</para>
	/// <para>该机制是线程安全的</para>
	/// </summary>
	/// <typeparam name="TKey"></typeparam>
	/// <typeparam name="TValue"></typeparam>
	/// <param name="valueFactory">为key创建value的方法</param>
	/// <param name="filter">过滤项，根据value的值确定是否抛弃，返回true表示不抛弃，返回false表示抛弃</param>
	/// <param name="comparer">可以为null,null代表使用默认的排序方式</param>
	/// <returns>确保valueFactory在每个key上只会调用一次,该方法是线程安全的</returns>

	public static <TKey,TValue> Function<TKey, TValue> init(
			Function<TKey, TValue> valueFactory, Function<TValue,Boolean> filter)
	{
            if (valueFactory == null) throw new ArgumentNullException("valueFactory");
            var data = new HashMap<TKey, TValue>();
            return key ->
            {
                TValue result;
                if (data.TryGetValue(key, out result)) return result;
                lock (data)
                {
                    if (data.TryGetValue(key, out result)) return result;
                    var newValue = valueFactory(key);
                    if (filter == null || filter(newValue))
                    {
                        if (data.TryGetValue(key, out result)) return result;  //为了防止valueFactory内进行了缓存，这里再次判断一下
                        data.Add(key, newValue);
                    }
                    return newValue;
                }
            };
        }
}
