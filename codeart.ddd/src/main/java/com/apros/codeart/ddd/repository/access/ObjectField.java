package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.metadata.PropertyMeta;

public abstract class ObjectField extends DataField {

	public ObjectField(PropertyMeta tip) {
		super(tip, DbType.Object, new DbFieldType[] {});
	}
}
