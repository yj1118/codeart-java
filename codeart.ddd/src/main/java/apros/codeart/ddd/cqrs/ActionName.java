package apros.codeart.ddd.cqrs;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

public final class ActionName {
	private ActionName() {
	}

	/**
	 * 
	 * 对象已更新的事件
	 * 
	 * @param type
	 * @return
	 */
	public static String getObjectMeta(Class<?> type) {
		return _getObjectMeta.apply(type.getSimpleName());
	}

	public static String getObjectMeta(String typeName) {
		return _getObjectMeta.apply(typeName);
	}

	private static Function<String, String> _getObjectMeta = LazyIndexer.init((String typeName) -> {
		return String.format("d:cqrs-getMeta-%s", typeName.toLowerCase());
	});

//	public static String getObject(Class<?> type) {
//		return _getObject.apply(type);
//	}
//
//	/// <summary>
//	/// 获取对象
//	/// </summary>
//	private static Function<Class<?>, String> _getObject = LazyIndexer.init((type) -> {
//		return String.format("d:get{0}", type.getSimpleName());
//	});

	/**
	 * 
	 * 对象已更新的事件
	 * 
	 * @param type
	 * @return
	 */
	public static String objectAdded(Class<?> type) {
		return _getObjectAdded.apply(type);
	}

	private static Function<Class<?>, String> _getObjectAdded = LazyIndexer.init((type) -> {
		return String.format("d:cqrs-%sAdded", StringUtil.firstToLower(type.getSimpleName()));
	});

	/**
	 * 
	 * 对象已更新的事件
	 * 
	 * @param type
	 * @return
	 */
	public static String objectUpdated(Class<?> type) {
		return _getObjectUpdated.apply(type);
	}

	private static Function<Class<?>, String> _getObjectUpdated = LazyIndexer.init((type) -> {
		return String.format("d:cqrs-%sUpdated", StringUtil.firstToLower(type.getSimpleName()));
	});

	/// <summary>
	/// 对象已删除
	/// </summary>
	/// <param name="type"></param>
	/// <returns></returns>
	public static String objectDeleted(Class<?> type) {
		return _getObjectDeleted.apply(type);
	}

	private static Function<Class<?>, String> _getObjectDeleted = LazyIndexer.init((type) -> {
		return String.format("d:cqrs-%sDeleted", StringUtil.firstToLower(type.getSimpleName()));
	});

}
