package apros.codeart.ddd.saga.internal.protector;

import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.util.StringUtil;

import java.util.function.Consumer;

/**
 * 大多数情况下不需要用它，
 * 这是因为StringBuilder是设计为轻量级、短期使用的对象，其性能已经相当高效。池化是将对象复用以减少频繁创建和销毁对象的开销，适用于某些重量级对象（如数据库连接、线程池等）。
 * 但是，StringBuilder并不属于此类对象。
 */
final class TracePool {
    private TracePool() {
    }

    private static final Pool<Trace> _pool = new Pool<Trace>(Trace.class, new PoolConfig(2, 100, 60),
            (isTempItem) -> {
                return new Trace();
            }, Trace::clear);

    public static void using(Consumer<Trace> action) {
        try (var temp = _pool.borrow()) {
            Trace trace = temp.getItem();
            action.accept(trace);
        }
    }
}
