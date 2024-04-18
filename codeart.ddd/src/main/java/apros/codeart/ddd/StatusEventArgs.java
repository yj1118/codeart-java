package apros.codeart.ddd;

import java.util.HashMap;
import java.util.Map;

import apros.codeart.i18n.Language;

public final class StatusEventArgs {

	private Map<String, Object> _data = new HashMap<String, Object>();

	StatusEventArgs() {
	}

	public void set(String name, Object value) {
		_data.put(name, value);
	}

	public <T> T get(String name) {
		return get(name, false);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String name, boolean throwError) {
		var value = _data.get(name);

		if (value != null) {
			return (T) value;
		}

		if (throwError)
			throw new DomainDrivenException(Language.strings("codeart.ddd", "NotFoundForStateEvent", name));
		return null;
	}

	public boolean contains(String name) {
		return _data.containsKey(name);
	}

	void clear() {
		_data.clear();
	}
}
