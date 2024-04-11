package com.apros.codeart.ddd.repository.access;

public class ByteField extends DbField {
	public Class<?> valueType() {
		return byte.class;
	}

	public ByteField(String name) {
		super(name);
	}
}
