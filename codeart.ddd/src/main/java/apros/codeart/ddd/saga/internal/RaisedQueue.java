package apros.codeart.ddd.saga.internal;

import java.util.List;

import apros.codeart.ddd.saga.RaisedEntry;

public final class RaisedQueue {

	private String _id;

	public String id() {
		return _id;
	}

	private List<RaisedEntry> _entries;

	public RaisedQueue(String id, List<RaisedEntry> entries) {
		_id = id;
		_entries = entries;
		_pointer = -1;
	}

	private int _pointer;

	public RaisedEntry next() {

		_pointer++;

		if (_pointer >= _entries.size())
			return null;

		return _entries.get(_pointer);
	}

}
