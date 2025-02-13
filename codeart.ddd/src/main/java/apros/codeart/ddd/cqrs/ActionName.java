package apros.codeart.ddd.cqrs;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

public final class ActionName {
    private ActionName() {
    }

    /**
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

    private static final Function<String, String> _getObjectMeta = LazyIndexer.init((String typeName) -> {
        return String.format("cqrs-getMeta-%s", typeName.toLowerCase());
    });

    public static String getObject(Class<?> type) {
        return _getObject.apply(type.getSimpleName());
    }

    public static String getObject(String typeName) {
        return _getObject.apply(typeName);
    }

    /// <summary>
    /// 获取对象
    /// </summary>
    private static final Function<String, String> _getObject = LazyIndexer.init((String typeName) -> {
        return String.format("cqrs-get-%s", typeName.toLowerCase());
    });

    public static String objectAdded(String typeName) {
        return _getObjectAdded.apply(typeName);
    }

    /**
     * 对象已更新的事件
     *
     * @param type
     * @return
     */
    public static String objectAdded(Class<?> type) {
        return _getObjectAdded.apply(type.getSimpleName());
    }

    private static Function<String, String> _getObjectAdded = LazyIndexer.init((typeName) -> {
        return String.format("cqrs-%sAdded", StringUtil.firstToLower(typeName));
    });

    public static String objectUpdated(Class<?> type) {
        return _getObjectUpdated.apply(type.getSimpleName());
    }

    /**
     * 对象已更新的事件
     *
     * @param type
     * @return
     */
    public static String objectUpdated(String typeName) {
        return _getObjectUpdated.apply(typeName);
    }

    private static final Function<String, String> _getObjectUpdated = LazyIndexer.init((typeName) -> {
        return String.format("cqrs-%sUpdated", StringUtil.firstToLower(typeName));
    });

    public static String objectDeleted(Class<?> type) {
        return objectDeleted(type.getSimpleName());
    }

    /// <summary>
    /// 对象已删除
    /// </summary>
    /// <param name="type"></param>
    /// <returns></returns>
    public static String objectDeleted(String typeName) {
        return _getObjectDeleted.apply(typeName);
    }

    private static final Function<String, String> _getObjectDeleted = LazyIndexer.init((typeName) -> {
        return String.format("cqrs-%sDeleted", StringUtil.firstToLower(typeName));
    });

}
