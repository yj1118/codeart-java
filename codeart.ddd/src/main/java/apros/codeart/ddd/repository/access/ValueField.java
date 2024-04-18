package apros.codeart.ddd.repository.access;

import static apros.codeart.i18n.Language.strings;

import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.util.PrimitiveUtil;

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
		validateType(tip);
	}

	private void validateType(PropertyMeta tip) {

		if (!PrimitiveUtil.is(tip.monotype()) && !tip.isEmptyable())
			throw new IllegalStateException(strings("codeart.ddd", "DomainObjectTypeWrong"));

	}

}
