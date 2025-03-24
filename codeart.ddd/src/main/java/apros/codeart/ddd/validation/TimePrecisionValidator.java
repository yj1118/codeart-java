package apros.codeart.ddd.validation;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.PropertyValidatorImpl;
import apros.codeart.ddd.ValidationResult;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.ScheduledActionType;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class TimePrecisionValidator extends PropertyValidatorImpl {

    public TimePrecisionValidator(TimePrecision tip) {
        super(tip);
    }

    public TimePrecisions precision() {
        return this.getTip(TimePrecision.class).value();
    }

    @Override
    protected void validate(DomainObject domainObject, PropertyMeta property, Object propertyValue,
                            ScheduledActionType actionType, ValidationResult result) {
        // 无须验证
    }

    @Override
    public DTObject getData() {
        // 请注意，数据格式要与注解的属性对应上
        DTObject data = DTObject.editable();
        data.setByte("precision", this.precision().getValue());
        return data;
    }
}
