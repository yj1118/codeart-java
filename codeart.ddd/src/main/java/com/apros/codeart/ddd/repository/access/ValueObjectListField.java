package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.metadata.PropertyMeta;

public class ValueObjectListField extends ObjectField {

	public DataFieldType fieldType() {
		return DataFieldType.ValueObjectList;
	}

	public boolean isMultiple() {
		return true;
	}

	public ValueObjectListField(PropertyMeta tip) {
		super(tip);
	}
}
