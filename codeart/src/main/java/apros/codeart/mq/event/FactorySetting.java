package apros.codeart.mq.event;

import java.util.function.Supplier;

import apros.codeart.i18n.Language;
import apros.codeart.mq.EasyMQException;

public class FactorySetting<T> {

	private Class<T> _type;

	private T _factory;

	public FactorySetting(Class<T> type, Supplier<T> getByConfig) {
		_type = type;
		_factory = getByConfig.get();
	}

	public void register(T factory) {
		if (_factory == null)
			_factory = factory;
	}

	public T getFactory() {
		if (_factory == null)
			throw new EasyMQException(Language.strings("NotFoundFactory", _type.getName()));
		return _factory;
	}

}
