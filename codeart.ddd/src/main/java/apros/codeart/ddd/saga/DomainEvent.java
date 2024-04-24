package apros.codeart.ddd.saga;

public abstract class DomainEvent implements IDomainEvent {

	public DomainEvent() {
	}

	private String _name;

	/**
	 * 
	 * 事件名称，在一个项目内全局唯一
	 * 
	 * @return
	 */
	public String name() {
		return _name;
	}

	private Iterable<String> _preEvents;

	/**
	 * 前置事件
	 * 
	 * @return
	 */
	public abstract Iterable<String> getPreEvents();

	/**
	 * 后置事件
	 * 
	 * @return
	 */
	public abstract Iterable<String> getPostEvents();

	/// <summary>
	/// 当前事件对应的条目
	/// </summary>
	internal EventEntry Entry
	{
	    get;
	    set;
	}

	/// <summary>
	/// 当前事件对应的条目
	/// </summary>
	// internal EventEntrySlim Entry
	// {
//	    get;
//	    set;
	// }

	/// <summary>
	/// 接受一个事件调用完成后的结果
	/// </summary>
	/// <returns></returns>
	internal

	void ApplyResult(EventEntry entry, DTObject result)
	{
	    var eventName = entry.EventName;
	    if (eventName.EqualsIgnoreCase(this.EventName))
	    {
	        //接受自身事件触发的结果
	        this.SetArgs(result);
	    }
	    else
	    {
	        entry.ArgsCode = result.GetCode();
	    }
	    this.EventCompleted(eventName, result);
	    UpdateCode();
	}

	/// <summary>
	/// 填充事件的参数
	/// </summary>
	/// <param name="eventName"></param>
	/// <param name="args"></param>
	protected virtual void FillArgs(string eventName, DTObject args)
	{

	}

	/// <summary>
	/// 事件执行完毕之后触发该回调
	/// </summary>
	/// <param name="preEventName"></param>
	/// <param name="result"></param>
	protected virtual void EventCompleted(string eventName, DTObject result)
	{

	}

	#endregion

	public void Raise() {
		RaiseImplement();
	}

	/// <summary>
	/// 实现执行事件的方法
	/// </summary>
	/// <returns>如果领域事件没有返回值，那么返回null</returns>
	protected abstract void RaiseImplement();

	public void Reverse() {
		ReverseImplement();
	}

	/// <summary>
	/// 实现回逆事件的方法
	/// </summary>
	protected abstract void ReverseImplement();

	#

	region 全局事件

	/// <summary>
	/// 领域事件被成功执行完毕的事件
	/// </summary>
	internal
	static event Action<Guid,DomainEvent>Succeeded;

	public static void OnSucceeded(Guid queueId, DomainEvent @event)
	{
	    if (Succeeded != null)
	        Succeeded(queueId, @event);
	}

	/// <summary>
	/// 表示领域事件执行失败，但是成功恢复状态（还原到执行领域事件之前的状态）
	/// </summary>
	internal

	static event Action<Guid,EventFailedException>Failed;

	public static void OnFailed(Guid queueId, EventFailedException reason) {
		if (Failed != null)
			Failed(queueId, reason);
	}

	/// <summary>
	/// 表示领域事件执行失败，并且没有成功恢复的事件
	/// </summary>
	internal

	static event Action<Guid,EventErrorException>Error;

	public static void OnError(Guid queueId, EventErrorException ex) {
		if (Error != null)
			Error(queueId, ex);
	}

	public static Exception OnErrorNoQueue(Guid eventId) {
		var ex = EventErrorException.CreateNoQueue(eventId);
		OnError(Guid.Empty, ex);
		return ex;
	}

	static void initialize() {
		EventLockRepository.Instance.Initialize();
		EventLogEntryRepository.Instance.Initialize();
		EventLogRepository.Instance.Initialize();
		EventMonitorRepository.Instance.Initialize();
		EventQueueRepository.Instance.Initialize();
	}

}
