package com.apros.codeart.runtime;

import static com.apros.codeart.i18n.Language.strings;

import java.util.LinkedList;

final class EvaluationStack {

	private LinkedList<Class<?>> _value_types;

	public Class<?>[] getValueTypes(int expectedCount) {
		var ts = new Class<?>[expectedCount];
		var i = expectedCount - 1;
		for (var vt : _value_types) {
			ts[i] = vt;
			i--;
		}
		return ts;
	}

	public void validateRefs(int expectedCount) {
		var types = this.getValueTypes(expectedCount);
		for (var type : types) {
			if (type.isPrimitive())
				throw new IllegalArgumentException(strings("TypeMismatch"));
		}
	}

	public EvaluationStack() {
		_value_types = new LinkedList<Class<?>>();
	}

	public void push(Class<?> type) {
		_value_types.push(type);
	}

	public Class<?> pop() {
		return _value_types.pop();
	}

	public int size() {
		return _value_types.size();
	}

}
