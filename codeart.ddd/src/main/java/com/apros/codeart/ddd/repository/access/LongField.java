package com.apros.codeart.ddd.repository.access;

public class LongField extends DbField {

	public Class<?> valueType() {
		return long.class;
	}

	public LongField(String name) {
		super(name);
	}
}
