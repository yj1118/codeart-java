package apros.codeart.ddd.validation;

import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.ScheduledActionType;
import apros.codeart.i18n.Language;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.INotNullObject;
import apros.codeart.ddd.PropertyValidatorImpl;
import apros.codeart.ddd.ValidationResult;
import apros.codeart.dto.DTObject;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.PrimitiveUtil;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
public class NotEmptyValidator extends PropertyValidatorImpl {

    public NotEmptyValidator(NotEmpty tip) {
        super(tip);
    }

    public boolean trim() {
        return this.getTip(NotEmpty.class).trim();
    }

    @Override
    protected void validate(DomainObject domainObject, PropertyMeta property, Object propertyValue,
                            ScheduledActionType actionType, ValidationResult result) {

        if (propertyValue == null) {
            addError(property, result);
            return;
        }

        INotNullObject notNullObj = TypeUtil.as(propertyValue, INotNullObject.class);
        if (notNullObj != null) {
            if (notNullObj.isEmpty()) {
                addError(property, result);
            }
            return;
        }

        if (property.isCollection()) {
            var listValue = (Iterable<?>) propertyValue;
            if (Iterables.size(listValue) == 0) {
                addError(property, result);
            }
            return;
        }

        if (property.monotype() == String.class) {
            var stringValue = (String) propertyValue;
            if (this.trim())
                stringValue = StringUtil.trim(stringValue);

            if (Strings.isNullOrEmpty(stringValue)) {
                addError(property, result);
            }
            return;
        }

        if (PrimitiveUtil.is(property.monotype())) {
            if (PrimitiveUtil.isDefaultValue(propertyValue)) {
                addError(property, result);
            }
            return;
        }

    }

    private void addError(PropertyMeta property, ValidationResult result) {
        result.append(property.name(), "NotEmpty", Language.strings("apros.codeart.ddd", "NotEmpty", property.call()));
    }

    public static final String ErrorCode = "NotEmptyError";

    @Override
    public DTObject getData() {
        // 请注意，数据格式要与注解的属性对应上
        DTObject data = DTObject.editable();
        data.setBoolean("trim", this.trim());
        return data;
    }
}
