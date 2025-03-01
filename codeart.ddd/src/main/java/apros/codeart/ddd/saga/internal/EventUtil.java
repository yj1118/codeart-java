package apros.codeart.ddd.saga.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import apros.codeart.ddd.saga.EventStatus;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

public final class EventUtil {

    private EventUtil() {
    }

    private static final Function<String, String> _getEffectiveName = LazyIndexer.init((eventName) -> {
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

    private static final Function<String, String> _getRaise = LazyIndexer.init((eventName) -> {
        eventName = _getEffectiveName.apply(eventName);
        return String.format("%sRaise", eventName);
    });

    private static final Function<String, String> _getRaiseResult = LazyIndexer.init((eventName) -> {
        eventName = _getEffectiveName.apply(eventName);
        return String.format("%sRaiseResult", eventName);
    });

    /**
     * 获得“回逆事件”的事件名称
     */
    private static final Function<String, String> _getReverse = LazyIndexer.init((eventName) -> {
        eventName = _getEffectiveName.apply(eventName);
        return String.format("%sReverse", eventName);
    });

    /**
     * 获得事件有效名称
     *
     * @param eventName
     * @return
     */
    public static String getEffectiveName(String eventName) {
        return _getEffectiveName.apply(eventName);
    }

    public static String getEventId(String queueId, String eventName, int index) {
        return String.format("%s-%s-%s", queueId, eventName, index);
    }
}
