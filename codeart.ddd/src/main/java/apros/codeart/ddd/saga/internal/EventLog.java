package apros.codeart.ddd.saga.internal;

import java.util.List;

import apros.codeart.ddd.saga.EventLogFactory;
import apros.codeart.ddd.saga.RaisedEntry;
import apros.codeart.dto.DTObject;

public final class EventLog {
    private EventLog() {
    }

    /**
     * 程序运行时的事件日志初始化
     */
    public static void init() {
        EventLogFactory.getFactory().init();
    }

    /**
     * 获得一个新的日志唯一标识（也就是事件队列编号）
     *
     * @return
     */
    public static String newId() {
        var log = EventLogFactory.createLog();
        return log.newId();
    }

    /**
     * 开始触发事件队列
     *
     * @param queueId
     */
    public static void writeRaiseStart(String queueId) {
        var log = EventLogFactory.createLog();
        log.writeRaiseStart(queueId);
    }

    /**
     * 写入要执行事件 {@eventName} 的日志
     *
     * @param queue
     * @param entry
     */
    public static void writeRaise(String queueId, String eventName, int entryIndex) {
        var log = EventLogFactory.createLog();
        log.writeRaise(queueId, eventName, entryIndex);
    }

    public static void writeRaiseLog(String queueId, String eventName, int entryIndex, DTObject log) {
        var logger = EventLogFactory.createLog();
        logger.writeRaiseLog(queueId, eventName, entryIndex, log);
    }

    /**
     * 写入事件已经全部触发完毕的日志
     *
     * @param queueId
     */
    public static void writeRaiseEnd(String queueId) {
        var log = EventLogFactory.createLog();
        log.writeRaiseEnd(queueId);
    }

    public static void writeReverseStart(String queueId) {
        var log = EventLogFactory.createLog();
        log.writeReverseStart(queueId);
    }

    /**
     * 得到已经执行了的事件队列（注意，最后执行的事件在队列的第一项）
     *
     * @param queueId
     * @return
     */
    public static RaisedQueue findRaised(String queueId) {
        var log = EventLogFactory.createLog();
        var entries = log.findRaised(queueId);
        if (entries == null || entries.isEmpty()) return null;
        return new RaisedQueue(queueId, entries);
    }

    /**
     * 记录事件已被回溯（对于本地事件，是成功回溯，对于远程事件，是已经成功发送回溯通知）
     *
     * @param queueId
     * @param eventId
     */
    public static void writeReversed(String queueId, RaisedEntry entry) {
        var log = EventLogFactory.createLog();
        log.writeReversed(queueId, entry);
    }

    /**
     * 记录事件已经全部回溯
     *
     * @param queueId
     */
    public static void writeReverseEnd(String queueId) {
        var log = EventLogFactory.createLog();
        log.writeReverseEnd(queueId);
    }

    /**
     * 找到由于中断的原因要恢复的事件队列编号
     *
     * @return
     */
    public static List<String> findInterrupteds() {
        var log = EventLogFactory.createLog();
        return log.findInterrupteds();
    }

    /**
     * 清理过期的日志
     */
    public static void clean() {
        var log = EventLogFactory.createLog();
        log.clean();
    }

}
