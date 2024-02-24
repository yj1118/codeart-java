package com.apros.codeart.runtime;

import static com.apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.ThreadSafe;

public final class FieldUtil {
	private FieldUtil() {
	}

	public static void each(Object obj, BiConsumer<String, Object> action) {
		// 获取目标类的 Class 对象
		Class<?> cls = obj.getClass();

		// 获取类声明的所有字段
		Field[] fields = cls.getDeclaredFields();

		try {
			// 遍历字段数组
			for (Field field : fields) {
				action.accept(field.getName(), field.get(obj));
			}
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	/**
	 * Field类本身并不具备线程安全性，
	 * 
	 * 但如果只是使用Field对象来获取字段的信息（例如字段的名称、类型等），那么通常不会存在线程安全问题。
	 */
	private static Function<Class<?>, Field[]> _getFields = LazyIndexer.init((cls) -> {
		return cls.getDeclaredFields();
	});

	/**
	 * 由于只是读信息，所以该方法线程安全
	 * 
	 * Memoized后缀表示以空间换时间
	 * 
	 * @param obj
	 * @param action
	 * @throws Exception
	 */
	@ThreadSafe
	public static void eachMemoized(Object obj, BiConsumer<String, Object> action) {
		// 获取目标类的 Class 对象
		Class<?> cls = obj.getClass();

		// 获取类声明的所有字段
		Field[] fields = _getFields.apply(cls);

		try {
			// 遍历字段数组
			for (Field field : fields) {
				action.accept(field.getName(), field.get(obj));
			}
		} catch (Exception e) {
			throw propagate(e);
		}
	}
}
