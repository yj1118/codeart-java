package apros.codeart.util.thread;

import apros.codeart.log.Logger;

import static apros.codeart.i18n.Language.strings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Timer {

    private final long _period;

    private final TimeUnit _unit;

    private ScheduledExecutorService _scheduler;

    private final LoopType _type;

    public Timer(long period, TimeUnit unit) {
        this(period, unit, LoopType.FixedDelay);
    }

    public Timer(long period, TimeUnit unit, LoopType type) {
        _period = period;
        _unit = unit;
        _type = type;
    }

    /**
     * 立即执行
     *
     * @param action
     */
    public void immediate(Runnable action) {
        start(0, action);
    }

    /**
     * 延迟执行
     *
     * @param initialDelay
     * @param action
     */
    public void delay(long initialDelay, Runnable action) {
        start(initialDelay, action);
    }

    /**
     * 延迟一个周期后开始反复执行
     *
     * @param action
     */
    public void delay(Runnable action) {
        start(_period, action);
    }

    private void start(long initialDelay, Runnable action) {
        if (_scheduler != null)
            throw new IllegalArgumentException(strings("apros.codeart", "TimerWorking"));
        // 创建基于虚拟线程的的ScheduledExecutorService
        var threadFactory = Thread.ofVirtual().factory();
        _scheduler = Executors.newScheduledThreadPool(1, threadFactory);
        if (_type == LoopType.FixedDelay) {
            _scheduler.scheduleWithFixedDelay(() -> {
                try {
                    action.run();
                } catch (Throwable e) {
                    Logger.error(e);
                }
            }, initialDelay, _period, _unit);
        } else {
            _scheduler.scheduleAtFixedRate(() -> {
                try {
                    action.run();
                } catch (Throwable e) {
                    Logger.error(e);
                }
            }, initialDelay, _period, _unit);
        }
    }

    public void stop() {
        if (_scheduler != null) {
            _scheduler.shutdown();
            _scheduler = null;
        }
    }

    public static enum LoopType {
        /**
         * 任务每完成一次后将等待指定时间再次开始
         */
        FixedDelay,

        /**
         * 固定时间触发
         */
        FixedRate,
    }

}
