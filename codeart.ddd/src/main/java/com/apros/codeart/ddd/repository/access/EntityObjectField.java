package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.metadata.PropertyMeta;

/**
 * 领域对象中引用对象的字段
 */
public class EntityObjectField extends ObjectField {
	public DataFieldType fieldType() {
		return DataFieldType.EntityObject;
	}

	public boolean isMultiple() {
		return false;
	}

	public EntityObjectField(PropertyMeta meta) {
		super(meta);
	}

}