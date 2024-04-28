package apros.codeart.ddd.saga;

import java.util.function.Consumer;

import apros.codeart.context.AppSession;
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

	EventContext(String id, DTObject input) {
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

	void direct(String eventName) {
		_eventName = eventName;
		_eventId = String.format("%s-%s", this.id().toString(), _eventName);
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
//		e.setString("id", this.id()); // 环境编号（也是队列编号）
		e.setString("eventId", this.eventId());
		e.setString("eventName", this.eventName());
		e.setObject("args", args);
		e.setObject("identity", this.getIdentity());
		return e;
	}

	DTObject getIdentity() {
		return AppSession.adaptIdentity();
	}

	void save() {
		// 此处真实写入文件
		// 当raise里报错了，这里虽然不会回退，但是由于幂等性，不会引起BUG
	}

	void load() {

	}

}
