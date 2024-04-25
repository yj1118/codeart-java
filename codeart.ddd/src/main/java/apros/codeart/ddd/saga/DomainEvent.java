package apros.codeart.ddd.saga;

import java.util.ArrayList;

import com.google.common.collect.ImmutableList;

import apros.codeart.dto.DTObject;

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

	private ImmutableList<String> _entries;

	public ImmutableList<String> entries() {
		return _entries;
	}

	void apply(String eventName, DTObject result) {
		this.eventCompleted(eventName, result);
	}

	public abstract DTObject getArgs(String eventName, DTObject input);

	/**
	 * 
	 * 事件执行完毕之后触发该回调
	 * 
	 * @param eventName
	 * @param result
	 */
	protected abstract void eventCompleted(String eventName, DTObject result);

//	#endregion

	/**
	 * 触发事件
	 */
	public abstract DTObject raise(DTObject arg, EventContext context);

	/**
	 * 回溯事件
	 */
	public abstract void reverse(EventContext context);

//	#

//	region 全局事件

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
