package apros.codeart.ddd.remotable;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

final class RemoteActionName {
	private RemoteActionName() {
	}

	public static String getObject(Class<?> type) {
		return _getObject.apply(type);
	}

	/// <summary>
	/// 获取对象
	/// </summary>
	private static Function<Class<?>, String> _getObject = LazyIndexer.init((type) -> {
		return String.format("d:get{0}", type.getSimpleName());
	});

	/// <summary>
	/// 对象已更新
	/// </summary>
	/// <param name="type"></param>
	/// <returns></returns>
	public static String ObjectUpdated(Class<?> type) {
		return _getObjectUpdated.apply(type);
	}

	private static Function<Class<?>, String> _getObjectUpdated = LazyIndexer.init((type) -> {
		return String.format("d:{0}Updated", StringUtil.firstToLower(type.getSimpleName()));
	});

	/// <summary>
	/// 对象已删除
	/// </summary>
	/// <param name="type"></param>
	/// <returns></returns>
	public static String ObjectDeleted(Class<?> type) {
		return _getObjectDeleted.apply(type);
	}

	private static Function<Class<?>, String> _getObjectDeleted = LazyIndexer.init((type) -> {
		return String.format("d:{0}Deleted", StringUtil.firstToLower(type.getSimpleName()));
	});

}
