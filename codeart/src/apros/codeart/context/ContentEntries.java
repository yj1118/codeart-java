package apros.codeart.context;

import java.util.HashMap;

import apros.codeart.pooling.IReusable;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;
import apros.codeart.pooling.Util;

public final class ContentEntries implements IReusable {

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

	public void clear() throws Exception {
		Util.stop(_data.values());
		_data.clear();
	}

	public static Pool<ContentEntries> Pool = new Pool<ContentEntries>(() -> {
		return new ContentEntries();
	}, PoolConfig.onlyMaxRemainTime(300));

}