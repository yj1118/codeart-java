package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.google.common.base.Strings;

public class ValueField extends DataField {

	@Override
	public DataFieldType fieldType() {
		return DataFieldType.Value;
	}

	@Override
	public boolean isMultiple() {
		return false;
	}

	public ValueField(PropertyMeta tip, DbFieldType... dbFieldTypes) {
		super(tip, DataTableUtil.getDbType(tip), dbFieldTypes);
		ValidateType(attribute);
	}

	private void ValidateType(PropertyRepositoryAttribute attribute) {
		if (!DataUtil.IsPrimitiveType(attribute.PropertyType) && !attribute.IsEmptyable) {
			throw new DomainDesignException(Strings.DomainObjectTypeWrong);
		}
	}

}
