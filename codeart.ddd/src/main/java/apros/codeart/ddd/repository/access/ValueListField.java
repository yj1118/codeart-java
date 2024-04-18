package apros.codeart.ddd.repository.access;

import apros.codeart.ddd.metadata.PropertyMeta;

public class ValueListField extends ObjectField {

	@Override
	public DataFieldType fieldType() {
		return DataFieldType.ValueList;
	}

	@Override
	public boolean isMultiple() {
		return true;
	}

	public Class<?> _valueType;

	/**
	 * 值的实际类型
	 * 
	 * @return
	 */
	public Class<?> valueType() {
		return _valueType;
	}

	public ValueListField(PropertyMeta tip) {
		super(tip);
		_valueType = tip.monotype();
	}
}
