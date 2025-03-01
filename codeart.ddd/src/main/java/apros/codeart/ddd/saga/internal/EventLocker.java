package apros.codeart.ddd.saga.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 事件锁，确保每个事件实例运行时得线程安全
 */
public final class EventLocker {

    private EventLocker() {
    }

    private static final ConcurrentHashMap<String, ReentrantLock> _lockers = new ConcurrentHashMap<>();

    public static <T> T lock(String eventId, Supplier<T> runnable) {
        var locker = _lockers.computeIfAbsent(eventId, k -> new ReentrantLock());

        locker.lock();
        try {
            return runnable.get();
        } finally {
            locker.unlock();
            cleanup(eventId, locker);
        }
    }

    private static void cleanup(String eventId, ReentrantLock locker) {
        if (locker.tryLock()) { // 确保没有其他线程在等待
            try {
                _lockers.remove(eventId, locker);
            } finally {
                locker.unlock();
            }
        }
    }
}
