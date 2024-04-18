package apros.codeart.util;

import java.util.function.Function;

import apros.codeart.runtime.Activator;

public final class SafeAccessImpl {

	private SafeAccessImpl() {
	}

	/**
	 * 检查类型是否为并发访问安全的
	 * 
	 * @param type
	 */
	public static void checkUp(Class<?> type) {
		var access = type.getAnnotation(SafeAccess.class);
		if (access == null)
			throw new TypeUnsafeAccessException(type);
	}

	public static void checkUp(Object obj) {
		checkUp(obj.getClass());
	}

	public static boolean isDefined(Class<?> type) {
		return type.getAnnotation(SafeAccess.class) != null;
	}

	public static boolean isDefined(Object obj) {
		return isDefined(obj.getClass());
	}

	private static Function<Class<?>, Object> _getSingleInstance = LazyIndexer.init((objType) -> {
		checkUp(objType);
		return Activator.createInstance(objType); // 设置true，表示就算是私有的构造函数也能匹配
	});

	/**
	 * 
	 * 将类型<paramref name="objType"/>以单例的形式创建为<typeparamref name="T"/>的实例
	 * 同一类型不会创建多份实例
	 * 
	 * @param <T>
	 * @param objType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createSingleton(Class<T> objType) {
		return (T) _getSingleInstance.apply(objType);
	}

	/**
	 * 
	 * 将类型objType 创建为 T 的实例 如果类型标记了并发访问安全标签，那么会自动以单例的形式创建对象，同一类型不会创建多份实例</para>
	 * 
	 * @param <T>
	 * @param objType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createInstance(Class<T> objType) {
		return isDefined(objType) ? createSingleton(objType) : (T) Activator.createInstance(objType);
	}

}
