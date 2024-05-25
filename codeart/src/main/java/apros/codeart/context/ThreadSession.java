package apros.codeart.context;

@AppSessionAccess
public final class ThreadSession implements IAppSession {
	private static ThreadLocal<ContentEntries> local = ThreadLocal.withInitial(ContentEntries::new);

	private ThreadSession() {
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

	public static final ThreadSession Instance = new ThreadSession();

}
