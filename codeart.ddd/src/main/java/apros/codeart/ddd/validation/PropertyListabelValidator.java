package apros.codeart.ddd.validation;

import apros.codeart.ddd.*;
import apros.codeart.ddd.repository.ScheduledActionType;
import apros.codeart.runtime.TypeUtil;

import java.lang.annotation.Annotation;

/**
 * 既能验证目标类型，也能验证当目标类型为集合的成员类型时的情况
 */
public abstract class PropertyListabelValidator extends PropertyValidatorImpl {

    public PropertyListabelValidator(Annotation tip) {
        super(tip);
    }

    @Override
    public void validate(IDomainObject domainObject, DomainProperty property, ScheduledActionType actionType, ValidationResult result) {
        // 属性验证在删除对象时不必验证
        if (actionType == ScheduledActionType.Delete) return;

        var obj = (DomainObject) domainObject;

        if (property.isCollection()) {
            var values = TypeUtil.as(obj.getValue(property), Iterable.class);
            for (var value : values) {
                validate(obj, property, value, actionType, result);
            }
        } else {
            var value = obj.getValue(property);
            validate(obj, property, value, actionType, result);
        }
    }

    protected abstract void validate(DomainObject domainObject, DomainProperty property, Object propertyValue, ScheduledActionType actionType,
                                     ValidationResult result);
}
