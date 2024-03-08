package com.apros.codeart.dto.serialization;

import static com.apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;

class SerializeMethod {

	private Method _method;

	public SerializeMethod(Method method) {
		_method = method;
	}

	public void invoke(Object instance, IDTOWriter writer) {
		try {
			_method.invoke(null, instance, writer);
		} catch (Exception e) {
			throw propagate(e);
		}
	}
}
