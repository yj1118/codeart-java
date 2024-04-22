package apros.codeart.dto.serialization.internal;

import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;

import apros.codeart.dto.IDTOReader;

class DeserializeMethod {
	private Method _method;

	public DeserializeMethod(Method method) {
		_method = method;
	}

	public void invoke(Object instance, IDTOReader reader) {
		try {
			_method.invoke(null, instance, reader);
		} catch (Exception e) {
			throw propagate(e);
		}
	}
}
