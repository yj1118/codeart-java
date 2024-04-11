package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.metadata.PropertyMeta;

public class AggregateRootField extends ObjectField {

	public DataFieldType fieldType() {
		return DataFieldType.AggregateRoot;
	}

	public boolean isMultiple() {
		return false;
	}

	public AggregateRootField(PropertyMeta meta) {
		super(meta);
	}
}
