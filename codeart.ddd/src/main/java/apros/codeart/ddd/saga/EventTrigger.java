package apros.codeart.ddd.saga;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.saga.internal.EventLog;
import apros.codeart.ddd.saga.internal.EventUtil;
import apros.codeart.ddd.saga.internal.protector.EventProtector;
import apros.codeart.ddd.saga.internal.trigger.EventQueue;
import apros.codeart.ddd.saga.internal.trigger.ReceiveResultEventHandler;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.event.EventPortal;
import apros.codeart.util.concurrent.ISignal;
import apros.codeart.util.concurrent.LatchSignal;

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
            EventLog.writeRaise(ctx.id(), ctx.eventName(), entryIndex); // 一定要确保日志先被正确得写入，否则会有BUG

            args = queue.transformResult(args, ctx);

            if (entry.local() != null) {
                // 本地事件，直接执行
                args = raiseLocalEvent(entry.local(), args, ctx);
            } else {
                args = raiseRemoteEvent(args, ctx);
            }

            if (ctx.isPropagationStopped()) break;
        }

        EventLog.writeRaiseEnd(queue.id()); // 指示恢复管理器事件队列的操作已经全部完成
        return args;
    }

    private static DTObject raiseLocalEvent(DomainEvent event, DTObject args, EventContext ctx) {
        return DataContext.newScope(() -> {
            return event.raise(args, ctx);
        });
    }

    private static DTObject raiseRemoteEvent(DTObject args, EventContext ctx) {

        // 先订阅触发事件的返回结果的事件
        subscribeRemoteEventResult(ctx.eventId());

        // 再发布“触发事件”的事件
        var raiseEventName = EventUtil.getRaise(ctx.eventName());

        var remoteArg = ctx.getEntryRemotable(args);
        EventPortal.publish(raiseEventName, remoteArg); // 触发远程事件就是发布一个“触发事件”的事件
        // 订阅者会收到消息后会执行触发操作

        ISignal<DTObject> signal = createSignal(ctx.eventId());

        try {
            // 等待远程调用的结果
            var output = signal.wait(300, TimeUnit.SECONDS);

            var error = output.getString("error", null);

            if (error != null) {
                throw new RemoteEventFailedException(error);
            }

            return output.getObject("args", DTObject.empty());

        } catch (Throwable ex) {
            throw ex;
        } finally {
            removeSignal(ctx.eventId());
            cleanupRemoteEventResult(ctx.eventId());
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
