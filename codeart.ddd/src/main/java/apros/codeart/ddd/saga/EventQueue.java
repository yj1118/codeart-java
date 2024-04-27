package apros.codeart.ddd.saga;

import java.util.ArrayList;

import apros.codeart.ddd.saga.internal.EventLoader;
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

	private ArrayList<EventEntry> _entries;

	public Iterable<EventEntry> entries() {
		return _entries;
	}

	private static ArrayList<EventEntry> getEntries(DomainEvent source, DTObject input) {
		ArrayList<EventEntry> entries = new ArrayList<EventEntry>();
		for (var name : source.getPreEvents(input)) {
			var e = createEntry(name, source);
			entries.add(e);
		}

		entries.add(new EventEntry(source.name(), source, true, source));

		for (var name : source.getPostEvents(input)) {
			var e = createEntry(name, source);
			entries.add(e);
		}

		return entries;
	}

	private static EventEntry createEntry(String name, DomainEvent source) {
		var local = EventLoader.find(name);
		if (local == null) {
			// 远程事件永远是被展开的
			return new EventEntry(name, null, true, source);
		}

		// 本地事件的来源就是自己local
		return new EventEntry(name, local, false, local);
	}

	private int _pointer;

	/**
	 * 
	 * 获得队列里下一条要执行的事件名称
	 * 
	 * @return
	 */
	public EventEntry next(DTObject args) {

		_pointer++;

		if (_pointer >= _entries.size())
			return null;

		var next = _entries.get(_pointer);

		if (!next.expanded()) {
			// 展开，由于远程事件一定是被展开的，所以这里肯定是本地事件
			var es = getEntries(next.local(), args);
			_entries.remove(_pointer);
			// 将展开的事件项，加入到主集合中
			for (var i = 0; i < es.size(); i++) {
				_entries.add(_pointer + i, es.get(i));
			}
			next = _entries.get(_pointer);
		}

		return next;
	}

	private boolean _byRemoteInvoke;

	public boolean byRemoteInvoke() {
		return _byRemoteInvoke;
	}

	public EventQueue(DomainEvent source, DTObject input, boolean byRemoteInvoke) {
		_instanceId = Guid.compact();
		_source = source;
		_byRemoteInvoke = byRemoteInvoke;
		_pointer = -1;
		_entries = getEntries(source, input);

	}

	public DTObject getArgs(DTObject args, EventContext ctx) {
		return this.source().getArgs(args, ctx);
	}
}
