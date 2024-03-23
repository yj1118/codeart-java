package com.apros.codeart.runtime;

import static com.apros.codeart.runtime.Util.propagate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.function.Function;

import com.apros.codeart.util.LazyIndexer;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;

public final class TypeUtil {
	private TypeUtil() {
	}

	public static String resolveName(Class<?> cls) {
		return cls.getName(); // 先用自带的，后面升级为可以识别泛型的名称
	}

	/**
	 * 对于泛型集合，只能获得成员的上限类型
	 * 
	 * @param collectionType
	 * @return
	 */
	public static Class<?> resolveElementType(Class<?> collectionType) {

		if (collectionType.isArray()) {
			return collectionType.getComponentType();
		}

		TypeToken<?> typeToken = TypeToken.of(collectionType);

		// 获取集合的类型上限
		TypeToken<?> elementTypeToken = typeToken.resolveType(collectionType.getTypeParameters()[0]);

		// 获取集合的成员类型
		Class<?> elementType = elementTypeToken.getRawType();

		return elementType;
	}

	public static boolean isCollection(Class<?> cls) {
		return cls.isArray() || Collection.class.isAssignableFrom(cls);
	}

	public static boolean isAbstract(Class<?> cls) {
		// 注意int等基类型在java里被标记成abstract,表示不能实例化
		// 但是我们这里是要知道对方是否真的为抽象的，所以得自己写算法转换下
		if (cls.isPrimitive())
			return false;
		int modifiers = cls.getModifiers();
		return Modifier.isAbstract(modifiers);
	}

	public static boolean isPublic(Member member) {
		int modifiers = member.getModifiers();
		return !Modifier.isPrivate(modifiers);
	}

	@SuppressWarnings("unchecked")
	public static <T> T as(Object obj, Class<T> cls) {
		if (cls.isInstance(obj))
			return (T) obj;
		return null;
	}

	public static boolean is(Object obj, Class<?> cls) {
		return cls.isInstance(obj);
	}

	public static boolean any(Object obj, Class<?>... clses) {
		for (var cls : clses) {
			if (cls.isInstance(obj))
				return true;
		}
		return false;
	}

	public static boolean is(Class<?> cls, Class<?> targetCls) {
		return targetCls.isAssignableFrom(cls);
	}

	/// <summary>
	/// 获得继承链
	/// </summary>
	/// <param name="type"></param>
	/// <returns></returns>
	public static Iterable<Class<?>> getInheriteds(Class<?> type) {
		return _getInheriteds.apply(type);
	}

	/**
	 * 类型的继承深度，直接从object继承而来的对象深度为1，每继承一个类，深度加1
	 * 
	 * @param type
	 * @return
	 */
	public static int getDepth(Class<?> type) {
		return Iterables.size(getInheriteds(type));
	}

	private static Function<Class<?>, Iterable<Class<?>>> _getInheriteds = LazyIndexer.init((objectType) -> {
		ArrayDeque<Class<?>> inheriteds = new ArrayDeque<>();

		var type = objectType;
		while (type.getSuperclass() != null) {
			inheriteds.add(type.getSuperclass());
			type = type.getSuperclass();
		}
		return inheriteds;
	});

	public static <A extends Annotation> boolean isDefined(Class<?> type, Class<A> annType) {
		return type.getAnnotation(annType) != null;
	}

	public static boolean exists(String className, ClassLoader classLoader) {
		String resourcePath = className.replace('.', '/') + ".class";
		return classLoader.getResource(resourcePath) != null;
	}

	public static boolean exists(String className) {
		return exists(className, getDefaultClassLoader());
	}

	public static ClassLoader getDefaultClassLoader() {
		var loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = ClassLoader.getSystemClassLoader();
		}
		return loader;
	}

	public static Class<?> getClass(String className) {

		return getClass(className, getDefaultClassLoader());
	}

	public static Class<?> getClass(String className, ClassLoader classLoader) {

		try {
			if (!exists(className, classLoader))
				return null;
			return classLoader.loadClass(className);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

}
