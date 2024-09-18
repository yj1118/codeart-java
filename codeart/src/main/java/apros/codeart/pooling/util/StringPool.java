package apros.codeart.pooling.util;

import java.util.function.Consumer;

import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.util.StringUtil;

/**
 * 大多数情况下不需要用它，
 * 这是因为StringBuilder是设计为轻量级、短期使用的对象，其性能已经相当高效。池化是将对象复用以减少频繁创建和销毁对象的开销，适用于某些重量级对象（如数据库连接、线程池等）。
 * 但是，StringBuilder并不属于此类对象。
 */
public final class StringPool {
    private StringPool() {
    }

    private static final Pool<StringBuilder> _pool = new Pool<StringBuilder>(StringBuilder.class, new PoolConfig(10, 200, 60),
            (isTempItem) -> {
                return new StringBuilder(100);
            }, StringUtil::clear);

    public static String using(Consumer<StringBuilder> action) {
        try (var temp = _pool.borrow()) {
            StringBuilder sb = temp.getItem();
            action.accept(sb);
            return sb.toString();
        }
    }
}
