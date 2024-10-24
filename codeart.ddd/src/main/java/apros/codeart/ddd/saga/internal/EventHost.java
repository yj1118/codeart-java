package apros.codeart.ddd.saga.internal;

import java.util.concurrent.TimeUnit;

import apros.codeart.App;
import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.ddd.saga.internal.protector.EventProtector;
import apros.codeart.ddd.saga.internal.protector.ReverseEventHandler;
import apros.codeart.ddd.saga.internal.trigger.RaiseEventHandler;
import apros.codeart.echo.event.EventPortal;
import apros.codeart.util.thread.Timer;

public final class EventHost {

    private EventHost() {
    }

//	#region 驻留和取消事件

    /**
     * 订阅触发事件
     *
     * @param event
     */
    private static void subscribeRaise(DomainEvent event) {

        var raiseName = EventUtil.getRaise(event.name());
        // 作为事件的提供方，我们订阅了触发事件，这样当外界发布了“触发事件”后，这里就可以收到消息并且执行事件
        EventPortal.scheduleSubscription(raiseName, RaiseEventHandler.Instance, true);
    }

    /**
     * 订阅回逆
     *
     * @param event
     */
    private static void subscribeReverse(DomainEvent event) {
        var reverseName = EventUtil.getReverse(event.name());
        EventPortal.scheduleSubscription(reverseName, ReverseEventHandler.Instance, true);
    }

    private static void cancelRaise(DomainEvent event) {

        var raiseName = EventUtil.getRaise(event.name());
        EventPortal.cancel(raiseName);
    }

    private static void cancelReverse(DomainEvent event) {
        var reverseName = EventUtil.getReverse(event.name());
        EventPortal.cancel(reverseName);
    }

    public static void initialize() {

        setupSAGA();

        EventLoader.load();

        // 领域事件初始化
        EventLog.init();

        // 订阅事件
        subscribeEvents();
    }

    private static void setupSAGA() {
        App.setup("saga");
    }

    public static void dispose() {
        // 取消订阅
        cancelEvents();
        endScheduleClean();
    }

//	#region 订阅/取消订阅事件

    private static void subscribeEvents() {
        var es = EventLoader.events();
        for (var e : es) {
            subscribeRaise(e);
            subscribeReverse(e);
        }
    }

    /// <summary>
    /// 取消订阅
    /// </summary>
    private static void cancelEvents() {
        var es = EventLoader.events();
        for (var e : es) {
            cancelRaise(e);
            cancelReverse(e);
        }
    }

    public static void initialized() {
        EventProtector.restoreInterrupted();
        startScheduleClean();
    }

////	region 事件的启用和禁用
//
//	/**
//	 * 开启事件，有些事件不是自动启动的，所以需要手工开启
//	 * 
//	 * @param string
//	 */
//	public static void EnableEvent(params string[] eventNames)
//	{
//	    var tips = EventAttribute.Tips;
//
//	foreach(var eventName in eventNames)
//	    {
//	        var tip = EventAttribute.GetTip(eventName, false);
//	        if (tip != null) tip.IsEnabled = true;
//	    }
//	}
//
//	/// <summary>
//	/// 禁用事件
//	/// </summary>
//	/// <param name="eventNames"></param>
//	public static void DisabledEvent(params string[] eventNames)
//	{
//	    var tips = EventAttribute.Tips;
//
//	foreach (var eventName in eventNames)
//	    {
//	        var tip = EventAttribute.GetTip(eventName, false);
//	        if (tip != null) tip.IsEnabled = false;
//	    }
//	}
//
//	#endregion
//
//	#

//	region 定时清理

    private static final Timer _scheduler = new Timer(1, TimeUnit.DAYS);

    private static void startScheduleClean() {
        _scheduler.immediate(() -> {
            EventLog.clean();
        });
    }

    private static void endScheduleClean() {
        _scheduler.stop();
    }

//	#endregion

}
