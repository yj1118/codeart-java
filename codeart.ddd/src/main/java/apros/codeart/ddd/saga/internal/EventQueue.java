package apros.codeart.ddd.saga.internal;

import java.util.UUID;

import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.dto.DTObject;

public class EventQueue {

	private UUID _instanceId;

	/**
	 * 
	 * 事件示例的编号
	 * 
	 * @return
	 */
	public UUID instanceId() {
		return _instanceId;
	}

	private DomainEvent _source;

	/**
	 * 
	 * 引起事件链调用的事件源
	 * 
	 * @return
	 */
	public DomainEvent source() {
		return _source;
	}

	private DTObject _state;

	public DTObject state() {
		return _state;
	}
	
	public 

}
