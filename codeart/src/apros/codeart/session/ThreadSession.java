package apros.codeart.session;

import apros.codeart.pooling.IPoolItem;
import apros.codeart.pooling.PoolingException;

@SessionAccess
public final class ThreadSession implements ISession {
	private static ThreadLocal<IPoolItem<ContentEntries>> local = new ThreadLocal<>();

	private ThreadSession() {
	}

	public void initialize() throws PoolingException {
		var item = ContentEntries.Pool.borrow();
		local.set(item);
	}

	public void clear() throws Exception {
		var item = local.get();
		if (item != null) {
			ContentEntries.Pool.back(item);
			local.remove();
		}
	}

	public Object getItem(String name) {
		return local.get().getItem().get(name);
	}

	public void setItem(String name, Object value) {
		local.get().getItem().set(name, value);
	}

	public boolean containsItem(String name) {
		return local.get().getItem().contains(name);
	}

	public boolean valid() {
		return local.get() != null;
	}

	public static final ThreadSession instance = new ThreadSession();

}
