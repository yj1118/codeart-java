package apros.codeart.ddd.saga;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

final class EventUtil {

	private EventUtil() {
	}

	private static Function<String, String> _getEffectiveName = LazyIndexer.init((eventName) -> {
		var pos = eventName.indexOf("@");
		if (pos == -1)
			return eventName;

		return StringUtil.substr(eventName, 0, pos);
	});

	/**
	 * 获得“触发事件”的事件名称
	 * 
	 * @param eventName
	 * @return
	 */
	public static String getRaise(String eventName) {
		return _getRaise.apply(eventName);
	}

	/**
	 * 
	 * 获取触发对方事件的结果的事件名称
	 * 
	 * @param eventId
	 * @return
	 */
	public static String getRaiseResult(String eventId) {
		return _getRaiseResult.apply(eventId);
	}

	public static String getReverse(String eventName) {
		return _getReverse.apply(eventName);
	}

	private static Function<String, String> _getRaise = LazyIndexer.init((eventName) -> {
		eventName = _getEffectiveName.apply(eventName);
		return String.format("{0}Raise", eventName);
	});

	private static Function<String, String> _getRaiseResult = LazyIndexer.init((eventName) -> {
		eventName = _getEffectiveName.apply(eventName);
		return String.format("{0}RaiseResult", eventName);
	});

	/**
	 * 获得“回逆事件”的事件名称
	 */
	private static Function<String, String> _getReverse = LazyIndexer.init((eventName) -> {
		eventName = _getEffectiveName.apply(eventName);
		return String.format("{0}Reverse", eventName);
	});

	/**
	 * 
	 * 获得事件有效名称
	 * 
	 * @param eventName
	 * @return
	 */
	public static String getEffectiveName(String eventName) {
		return _getEffectiveName.apply(eventName);
	}
}
