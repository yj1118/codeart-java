package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.metadata.PropertyMeta;

public class EntityObjectListField extends ObjectField {

	public DataFieldType fieldType() {
		return DataFieldType.EntityObjectList;
	}

	public boolean isMultiple() {
		return true;
	}

	public EntityObjectListField(PropertyMeta tip) {
		super(tip);
	}
}
