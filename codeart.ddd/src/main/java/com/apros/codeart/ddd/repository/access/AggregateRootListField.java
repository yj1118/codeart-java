package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.metadata.PropertyMeta;

public class AggregateRootListField extends ObjectField {

	public DataFieldType fieldType() {
		return DataFieldType.AggregateRootList;
	}

	public boolean isMultiple() {
		return true;
	}

	public AggregateRootListField(PropertyMeta tip) {
		super(tip);
	}

}
