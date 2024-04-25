package apros.codeart.ddd.saga;

import java.util.function.Consumer;

import apros.codeart.dto.DTObject;

public final class EventContext {

	private String _id;

	private String _eventName;

	private DTObject _log;

	EventContext(String id, String eventName) {
		_id = id;
		_eventName = eventName;
	}

	public void write(Consumer<DTObject> action) {
		if (_log == null)
			_log = DTObject.editable();
		action.accept(_log);
	}

	void save() {
		// 此处真实写入文件
		// 当raise里报错了，这里虽然不会回退，但是由于幂等性，不会引起BUG
	}

	void load() {

	}

}
