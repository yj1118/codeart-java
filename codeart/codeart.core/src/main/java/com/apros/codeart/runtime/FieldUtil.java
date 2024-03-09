package com.apros.codeart.runtime;

import static com.apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.apros.codeart.i18n.Language;
import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.StringUtil;
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

	public static class Accesser {
		private Field _field;

		public Field getField() {
			return _field;
		}

		private Method _method;

		public Method getMethod() {
			return _method;
		}

		public Accesser(Field field) {
			_field = field;
		}

		public Accesser(Method method) {
			_method = method;
		}

		public boolean isField() {
			return _field != null;
		}

		public boolean isMethod() {
			return _method != null;
		}

	}

	/**
	 * 得到约定读取字段的信息的方法
	 */
	public static Accesser getFieldGetterMemoized(Class<?> objClass, String fieldName) {

		var field = getAgreeField(objClass, fieldName);

		if (canRead(field))
			return new Accesser(field);
		else {
			// 如果没有访问权限，那么尝试用访问方法
			var name = getAgreeName(fieldName);
			String methodName = String.format("get%s", StringUtil.firstToUpper(name));
			var method = MethodUtil.resolveMemoized(objClass, methodName);

			if (method == null || !canRead(method))
				throw new IllegalStateException(Language.strings("FieldNotFound", fieldName));

			return new Accesser(method);
		}
	}

	private static Field getAgreeField(Class<?> objClass, String fieldAgreeName) {
		// 把类似 _name得名称，改为name

		var fields = _getFields.apply(objClass);
		var target = ListUtil.find(fields, (f) -> {
			return f.getName().equalsIgnoreCase(fieldAgreeName);
		});

		if (target == null) {
			// 再找一次 _name
			var aName = String.format("_%s", fieldAgreeName);
			target = ListUtil.find(fields, (f) -> {
				return f.getName().equalsIgnoreCase(aName);
			});
		}

		if (target == null)
			throw new IllegalStateException(Language.strings("FieldNotFound", fieldAgreeName));

		return target;
	}

	private static Function<String, String> _getAgreeName = LazyIndexer.init((fieldName) -> {
		return fieldName.startsWith("_") ? fieldName.substring(1) : fieldName;
	});

	/**
	 * 获得字段的约定名
	 * 
	 * @param fieldName
	 * @return
	 */
	public static String getAgreeName(String fieldName) {
		return _getAgreeName.apply(fieldName);
	}

	public static String getAgreeName(Field field) {
		return _getAgreeName.apply(field.getName());
	}

	public static Accesser getFieldWriterMemoized(Class<?> objClass, String fieldName) {

		var field = getAgreeField(objClass, fieldName);

		if (canWrite(field))
			return new Accesser(field);
		else {
			// 如果没有访问权限，那么尝试用访问方法
			var name = getAgreeName(fieldName);
			String methodName = String.format("set%s", StringUtil.firstToUpper(name));
			var method = MethodUtil.resolveMemoized(objClass, methodName, field.getType());

			if (method == null || !canRead(method))
				throw new IllegalStateException(Language.strings("FieldNotFound", fieldName));

			return new Accesser(method);
		}
	}

	public static boolean canRead(Member member) {
		int modifiers = member.getModifiers();
		return !Modifier.isPrivate(modifiers);
	}

	public static boolean canWrite(Member member) {
		int modifiers = member.getModifiers();
		return !Modifier.isPrivate(modifiers) && !Modifier.isFinal(modifiers);
	}

}
