package apros.codeart.ddd.saga;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.saga.internal.EventLog;
import apros.codeart.ddd.saga.internal.EventLocker;
import apros.codeart.ddd.saga.internal.EventUtil;
import apros.codeart.ddd.saga.internal.protector.EventProtector;
import apros.codeart.ddd.saga.internal.trigger.EventQueue;
import apros.codeart.ddd.saga.internal.trigger.ReceiveResultEventHandler;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.event.EventPortal;
import apros.codeart.log.Logger;
import apros.codeart.util.concurrent.ISignal;
import apros.codeart.util.concurrent.LatchSignal;
import apros.codeart.util.concurrent.SignalTimeoutException;

public final class EventTrigger {

    private EventTrigger() {
    }

    /**
     * 开始一个新的事件
     *
     * @param source
     * @param input
     * @return
     */
    public static DTObject start(DomainEvent source, DTObject input) {
        var queue = new EventQueue(EventLog.newId(), source, input);
        try {
            return raise(queue);
        } catch (Throwable ex) {
            EventProtector.restore(queue.id()); // 恢复
            throw ex;
        }
    }

    /**
     * 开始一个新的事件
     *
     * @param queue
     * @return
     */
    public static DTObject raise(EventQueue queue) {
        var input = queue.input();
        DTObject args = input.toEditable();
        EventContext ctx = new EventContext(queue.id(), input);

        EventLog.writeRaiseStart(queue.id());

        while (true) {
            // 触发队列事件
            var entry = queue.next(args);
            if (entry == null) {
                args = queue.transformResult(args, ctx);  //转换一次结果,就将此结果返回给外部作为事件执行完毕后的结果
                break;
            }

            var entryIndex = queue.entryIndex();

            String eventName = entry.name();
            ctx.direct(eventName, entryIndex); // 将事件上下文重定向到新的事件上
            Logger.trace("saga", "%s - writeRaiseBefore", eventName);
            EventLog.writeRaise(ctx.id(), ctx.eventName(), entryIndex); // 一定要确保日志先被正确得写入，否则会有BUG
            Logger.trace("saga", "%s - writeRaiseAfter", eventName);

            args = queue.transformResult(args, ctx);

            if (entry.local() != null) {
                // 本地事件，直接执行
                Logger.trace("saga", "%s - raiseLocalEventBefore", ctx.eventId());
                args = raiseLocalEvent(entry.local(), args, ctx);
                Logger.trace("saga", "%s - raiseLocalEventAfter", ctx.eventId());

            } else {
                Logger.trace("saga", "%s - raiseRemoteEventBefore", ctx.eventId());
                args = raiseRemoteEvent(args, ctx);
                Logger.trace("saga", "%s - raiseRemoteEventAfter", ctx.eventId());
            }

            if (ctx.isPropagationStopped()) break;
        }

        EventLog.writeRaiseEnd(queue.id()); // 指示恢复管理器事件队列的操作已经全部完成
        return args;
    }

    private static DTObject raiseLocalEvent(DomainEvent event, DTObject args, EventContext ctx) {
        var eventId = ctx.eventId();
        return EventLocker.lock(ctx.eventId(), () -> {
            try {
                var status = EventStatus.getStatus(eventId);    //检查初始状态
                if (status != EventStatus.None) throw new IllegalStateException("event status not none");

                EventStatus.setStatus(ctx.eventId(), EventStatus.Raising);  // 设置为执行中

                var result = DataContext.newScope(() -> {
                    return event.raise(args, ctx);
                });

                if (EventStatus.getStatus(ctx.eventId()) != EventStatus.Raising) {
                    // 如果执行完毕了，结果状态不为Raising
                    // 那就是因为调用方超时，取消了调用，所以需要回溯
                    // 抛出异常引起回溯
                    throw new IllegalStateException("event status not raising");
                }
                return result;
            } finally {
                EventStatus.removeStatus(eventId);
            }
        });
    }

    private static DTObject raiseRemoteEvent(DTObject args, EventContext ctx) {

        // 先订阅触发事件的返回结果的事件
        subscribeRemoteEventResult(ctx.eventId());

        // 再发布“触发事件”的事件
        var raiseEventName = EventUtil.getRaise(ctx.eventName());

        var remoteArg = ctx.getEntryRemotable(args);
        Logger.trace("saga", "%s - publishBefore", ctx.eventId());
        EventPortal.publish(raiseEventName, remoteArg); // 触发远程事件就是发布一个“触发事件”的事件
        // 订阅者会收到消息后会执行触发操作
        Logger.trace("saga", "%s - publishAfter", ctx.eventId());

        ISignal<DTObject> signal = createSignal(ctx.eventId());

        try {
            // 等待远程调用的结果
            Logger.trace("saga", "%s - waitResponseBefore", ctx.eventId());
            var output = signal.wait(SAGAConfig.eventTimeout(), TimeUnit.SECONDS);
            Logger.trace("saga", "%s - waitResponseAfter", ctx.eventId());

            var error = output.getString("error", null);

            if (error != null) {
                Logger.trace("saga", "%s - error", ctx.eventId());
                throw new RemoteEventFailedException(error);
            }

            return output.getObject("args", DTObject.empty());

        } catch (SignalTimeoutException ex) {
            Logger.trace("saga", "%s - timeout:%s", ctx.eventId(), SAGAConfig.eventTimeout());
            throw ex;
        } catch (Throwable ex) {
            Logger.trace("saga", "%s - throwError", ctx.eventId());
            throw ex;
        } finally {
            removeSignal(ctx.eventId());
            cleanupRemoteEventResult(ctx.eventId());
            Logger.trace("saga", "%s - completed", ctx.eventId());
        }
    }

    private static void subscribeRemoteEventResult(String eventId) {
        var raiseResultEventName = EventUtil.getRaiseResult(eventId);
        // 单方面接收返回值，不需要集群支持
        EventPortal.subscribe(raiseResultEventName, ReceiveResultEventHandler.Instance, false);
    }

    /**
     * 删除由于接受调用结果而创建的临时队列
     *
     * @param eventId
     */
    public static void cleanupRemoteEventResult(String eventId) {
        var raiseResultEventName = EventUtil.getRaiseResult(eventId);
        EventPortal.remove(raiseResultEventName);
    }

    /**
     * 接收到远程调用后的返回结果，继续触发事件
     *
     * @param eventId
     * @param e
     */
    public static void continueRaise(String eventId, DTObject e) {
        var signal = _signals.get(eventId);
        if (signal != null)
            signal.set(e);
    }

    private static final ConcurrentHashMap<String, ISignal<DTObject>> _signals = new ConcurrentHashMap<>();

    private static ISignal<DTObject> createSignal(String eventId) {
        LatchSignal<DTObject> signal = new LatchSignal<>();
        _signals.put(eventId, signal);
        return signal;
    }

    private static void removeSignal(String eventId) {
        _signals.remove(eventId);
    }

    public static ISignal<DTObject> getSignal(String eventId) {
        return _signals.get(eventId);
    }
}
