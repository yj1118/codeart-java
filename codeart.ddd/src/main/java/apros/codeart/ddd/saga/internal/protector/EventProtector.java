package apros.codeart.ddd.saga.internal.protector;

import apros.codeart.context.AppSession;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.ddd.saga.internal.EventLog;
import apros.codeart.ddd.saga.internal.EventUtil;
import apros.codeart.ddd.saga.internal.RaisedQueue;
import apros.codeart.ddd.saga.EventTrigger;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.event.EventPortal;
import apros.codeart.log.Logger;

public final class EventProtector {
    private EventProtector() {
    }

    public static void restore(String queueId) {

        Trace trace = new Trace(queueId);

        try {

            EventLog.writeReverseStart(queueId);

            var queue = EventLog.findRaised(queueId);

            // 已经没有数据了，表示不需要回溯
            if (queue == null)
                return;

            while (true) {
                // 触发回溯序列事件
                var entry = queue.next();
                if (entry == null) break;

                trace.start(entry);
                if (entry.local() != null) {
                    reverseLocalEvent(entry.local(), entry.log());
                } else {
                    reverseRemoteEvent(entry.name(), entry.index(), queue);
                }

                EventLog.writeReversed(queueId, entry);
                trace.end(entry);
            }

            EventLog.writeReverseEnd(queueId); // 指示恢复管理器事件队列的操作已经全部完成
            trace.end();

        } catch (Throwable ex) {
            // 恢复期间发生了错误，写入故障转移，留待管理员处理
            Failover.write(trace);
        }
    }

    private static void reverseLocalEvent(DomainEvent event, DTObject log) {
        DataContext.newScope(() -> {
            event.reverse(log);
        });
    }

    private static void reverseRemoteEvent(String eventName, int entryIndex, RaisedQueue queue) {
        var eventId = EventUtil.getEventId(queue.id(), eventName, entryIndex);
        // 调用远程事件时会创建一个接收结果的临时队列，有可能该临时队列没有被删除，所以需要在回逆的时候处理一次
        EventTrigger.cleanupRemoteEventResult(eventId);

        // 发布“回逆事件”的事件
        var reverseEventName = EventUtil.getReverse(eventName);

        var remotable = DTObject.editable();
        remotable.setString("id", queue.id());

        EventPortal.publish(reverseEventName, remotable);

        // 注意，与“触发事件”不同，我们不需要等待回逆的结果，只用传递回逆的消息即可
    }

    /**
     * 恢复中断的事件（用于机器故障，断电等意外情况）
     */
    public static void restoreInterrupted() {
        try {

            var queueIds = EventLog.findInterrupteds();

            if (queueIds == null || queueIds.isEmpty())
                return;

            queueIds.parallelStream().forEach(queueId -> {
                AppSession.using(() -> {
                    restore(queueId);
                });
            });

        } catch (Throwable ex) {
            Logger.error(ex);
        }
    }

}
