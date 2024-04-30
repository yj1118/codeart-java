package apros.codeart.ddd.saga.internal;

import apros.codeart.ddd.metadata.MetadataLoader;
import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.ddd.saga.internal.protector.EventProtector;
import apros.codeart.ddd.saga.internal.protector.ReverseEventHandler;
import apros.codeart.ddd.saga.internal.trigger.RaiseEventHandler;
import apros.codeart.mq.event.EventPortal;

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
		EventPortal.subscribe(raiseName, RaiseEventHandler.instance);
	}

	/**
	 * 
	 * 订阅回逆
	 * 
	 * @param event
	 */
	private static void subscribeReverse(DomainEvent event) {
		var reverseName = EventUtil.getReverse(event.name());
		EventPortal.subscribe(reverseName, ReverseEventHandler.instance);
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

		EventLoader.load();

		// 领域事件初始化
		EventLog.init();

		// 订阅事件
		subscribeEvents();
	}

	public static void cleanup() {
		// 取消订阅
		cancelEvents();
		clearTimer();
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
		initTimer();
	}

	#endregion

	#

	region 事件的启用和禁用

	/// <summary>
	/// 开启事件，有些事件不是自动启动的，所以需要手工开启
	/// </summary>
	/// <param name="eventNames"></param>
	public static void EnableEvent(params string[] eventNames)
	{
	    var tips = EventAttribute.Tips;

	foreach(var eventName in eventNames)
	    {
	        var tip = EventAttribute.GetTip(eventName, false);
	        if (tip != null) tip.IsEnabled = true;
	    }
	}

	/// <summary>
	/// 禁用事件
	/// </summary>
	/// <param name="eventNames"></param>
	public static void DisabledEvent(params string[] eventNames)
	{
	    var tips = EventAttribute.Tips;

	foreach (var eventName in eventNames)
	    {
	        var tip = EventAttribute.GetTip(eventName, false);
	        if (tip != null) tip.IsEnabled = false;
	    }
	}

	#endregion

	#

	region 定时清理

	private static Timer _timer;

	private static void InitTimer() {
		_timer = new Timer(24 * 3600 * 1000); // 每间隔24小时执行一次
		_timer.Elapsed += OnElapsed;
		_timer.AutoReset = false;// 设置是执行一次（false）还是一直执行(true)
		_timer.Enabled = true;// 是否执行System.Timers.Timer.Elapsed事件
	}

	private static void ClearTimer() {
		_timer.Close();
		_timer.Dispose();
	}

	private static void OnElapsed(object sender, ElapsedEventArgs e) {
		try {
			Clear();
		} catch (Exception ex) {
			Logger.Fatal(ex);
		} finally {
			_timer.Start();
		}
	}

	/// <summary>
	/// 移除超过24小时已完成的事件锁、事件监视器、队列信息
	/// 我们不能在执行完领域事件后立即删除这些信息，因为有可能是外界调用本地的事件，稍后可能外界要求回逆事件
	/// 因此我们只删除24小时过期的信息，因为外界不可能过了24小时后又要求回逆
	/// </summary>
	/// <param name="minutes"></param>
	private static void Clear()
	{
	    DataContext.NewScope(() =>
	    {
	        var repository = EventLockRepository.Instance;
	        var locks = repository.FindExpireds(24);
	        foreach (var @lock in locks)
	        {
	            var queueId = @lock.Id;

	            var queue = EventQueue.Find(queueId);
	            if (!queue.IsSucceeded) continue; //对于没有执行成功的队列，我们不删除日志等信息，这样管理员可以排查错误

	            DataContext.NewScope(() =>
	            {
	                var monitor = EventMonitor.Find(queueId);
	                if (!monitor.IsEmpty()) EventMonitor.Delete(monitor);

	                EventQueue.Delete(queueId);
	                EventLogEntry.Deletes(queueId); //删除日志的所有条目
	                EventLog.Delete(queueId);
	            });
	            EventLock.Delete(@lock);
	        }
	    });
	   
	}

	#endregion

}
