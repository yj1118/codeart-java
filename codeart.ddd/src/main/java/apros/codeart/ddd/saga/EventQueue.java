package apros.codeart.ddd.saga;

import apros.codeart.context.AppSession;
import apros.codeart.dto.DTObject;
import apros.codeart.util.Guid;

public class EventQueue {

	public String id() {
		return this.instanceId();
	}

	private String _instanceId;

	/**
	 * 
	 * 事件示例的编号
	 * 
	 * @return
	 */
	public String instanceId() {
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

	private int _pointer;

	/**
	 * 
	 * 获得队列里下一条要执行的事件名称
	 * 
	 * @return
	 */
	public String next() {

		_pointer++;

		var es = _source.entries();
		if (_pointer >= es.size())
			return null;

		return es.get(_pointer);
	}

	public EventQueue(DomainEvent source) {
		_instanceId = Guid.compact();
		_source = source;
		_pointer = -1;

	}

	public boolean isLocal(String eventName) {
		return this.source().name().equals(eventName);
	}

	public DTObject getArgs(String eventName, DTObject input) {
		return this.source().getArgs(eventName, input);
	}

	public EventContext createContext(String eventName) {
		return new EventContext(this.id(), eventName);
	}

	public DTObject getIdentity() {
		return AppSession.adaptIdentity();
	}

	public String getEventId(String eventName) {
		return String.format("%s-%s", this.id().toString(), eventName);
	}

}
