package apros.codeart.context;

@AppSessionAccess
public final class ThreadSession implements IAppSession {
	private static ThreadLocal<ContentEntries> local = new ThreadLocal<>();

	private ThreadSession() {
	}

	public void initialize() {
		if (this.valid())
			return;
		var item = new ContentEntries();
		local.set(item);
	}

	public void clear() {
		var item = local.get();
		if (item != null) {
			item.clear();
			local.remove();
		}
	}

	public Object getItem(String name) {
		return local.get().get(name);
	}

	public void setItem(String name, Object value) {
		local.get().set(name, value);
	}

	public boolean containsItem(String name) {
		return local.get().contains(name);
	}

	public boolean valid() {
		return local.get() != null;
	}

	public static final ThreadSession Instance = new ThreadSession();

}
