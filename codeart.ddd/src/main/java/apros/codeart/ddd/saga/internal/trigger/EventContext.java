package apros.codeart.ddd.saga.internal.trigger;

import java.util.function.Consumer;

import apros.codeart.ddd.saga.internal.EventLog;
import apros.codeart.ddd.saga.internal.EventUtil;
import apros.codeart.dto.DTObject;

public final class EventContext {

	private String _id;

	public String id() {
		return _id;
	}

	private String _eventName;

	public String eventName() {
		return _eventName;
	}

	private DTObject _log;

	private DTObject _input;

	/**
	 * 事件最初的输入
	 * 
	 * @return
	 */
	public DTObject input() {
		return _input;
	}

	public EventContext(String id, DTObject input) {
		_id = id;
		_input = input;
	}

	public void write(Consumer<DTObject> action) {
		if (_log == null)
			_log = DTObject.editable();
		action.accept(_log);
	}

	private String _eventId;

	public String eventId() {
		return _eventId;
	}

	private int _index;

	public int index() {
		return _index;
	}

	/**
	 * @param eventName
	 * @param index     事件被执行的序号
	 */
	public void direct(String eventName, int index) {
		_eventName = eventName;
		_index = index;
		_eventId = EventUtil.getEventId(this.id(), _eventName, this.index());
		_log = null;
	}

	/**
	 * 
	 * 获得要执行事件得远程数据
	 * 
	 * @param eventName
	 * @param eventId
	 * @param args
	 * @return
	 */
	DTObject getEntryRemotable(DTObject args) {
		var e = DTObject.editable();
		e.setString("id", this.id()); // 环境编号（也是队列编号）
		e.setString("eventId", this.eventId());
		e.setString("eventName", this.eventName());
		e.setObject("args", args);
		return e;
	}

	void save() {
		if (_log != null)
			EventLog.writeRaiseLog(_id, _eventName, _index, _log);
	}
}
