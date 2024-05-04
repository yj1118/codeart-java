package apros.codeart.ddd.cqrs.internal;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

final class ActionName {
	private ActionName() {
	}

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
		return String.format("d:cqrs-{0}Added", StringUtil.firstToLower(type.getSimpleName()));
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
		return String.format("d:cqrs-{0}Updated", StringUtil.firstToLower(type.getSimpleName()));
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
		return String.format("d:cqrs-{0}Deleted", StringUtil.firstToLower(type.getSimpleName()));
	});

}
