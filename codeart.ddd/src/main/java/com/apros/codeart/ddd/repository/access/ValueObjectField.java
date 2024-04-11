package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.metadata.PropertyMeta;

public class ValueObjectField extends ObjectField {

	public DataFieldType fieldType() {
		return DataFieldType.ValueObject;
	}

	public boolean isMultiple() {
		return false;
	}

	public ValueObjectField(PropertyMeta meta) {
		super(meta);
	}
}
