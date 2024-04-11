package com.apros.codeart.ddd.repository.access;

public class BooleanField extends DbField {

	public Class<?> valueType() {
		return boolean.class;
	}

	public BooleanField(String name) {
		super(name);
	}
}
