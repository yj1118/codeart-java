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
public class NumericValidator extends PropertyValidatorImpl {

    public NumericValidator(Numeric tip) {
        super(tip);
    }

    public int precision() {
        return this.getTip(Numeric.class).precision();
    }

    public int scale() {
        return this.getTip(Numeric.class).scale();
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
        data.setInt("precision", this.precision());
        data.setInt("scale", this.scale());
        return data;
    }
}
