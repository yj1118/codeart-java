package apros.codeart.ddd.repository.access;

import static apros.codeart.i18n.Language.strings;

import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.dto.DTObject;
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

        if (!PrimitiveUtil.is(tip.monotype()) && !tip.monotype().isEnum() && !tip.isEmptyable() && !tip.monotype().equals(DTObject.class))
            throw new IllegalStateException(strings("apros.codeart.ddd", "DomainObjectTypeWrong"));

    }
}
