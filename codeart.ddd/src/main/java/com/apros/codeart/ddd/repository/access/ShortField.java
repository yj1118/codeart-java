package com.apros.codeart.ddd.repository.access;

public class ShortField extends DbField {

	public Class<?> valueType() {
		return short.class;
	}

	public ShortField(String name) {
		super(name);
	}
}
