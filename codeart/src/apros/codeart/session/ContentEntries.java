package apros.codeart.session;

import java.util.HashMap;

import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.pooling.PoolItemPhase;

public final class ContentEntries {

	private HashMap<String, Object> _data = new HashMap<String, Object>();

	public ContentEntries() {

	}

	public Object get(String name) {
		return _data.getOrDefault(name, null);
	}

	public void set(String name, Object value) {
		_data.put(name, value);
	}

	public boolean contains(String name) {
		return _data.containsKey(name);
	}

	public void Clear() {
		_data.clear();
	}

	public static Pool<ContentEntries> Pool = new Pool<ContentEntries>(() -> {
		return new ContentEntries();
	}, (obj, phase) -> {
		if (phase == PoolItemPhase.Returning) {
			obj.Clear();
		}
		return true;
	}, PoolConfig.onlyMaxRemainTime(300));

}
