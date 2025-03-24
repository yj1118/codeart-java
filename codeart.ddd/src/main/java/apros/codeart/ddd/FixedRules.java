package apros.codeart.ddd;

import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.ScheduledActionType;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class FixedRules implements IFixedRules {

    private FixedRules() {
    }

    public ValidationResult validate(IDomainObject obj, ScheduledActionType actionType) {
        ValidationResult result = ValidationResult.create();
        validateProperties(obj, actionType, result); // 先验证属性
        validateObject(obj, actionType, result); // 再验证对象
        return result;
    }

//	#region 验证属性

    private void validateProperties(IDomainObject obj, ScheduledActionType actionType, ValidationResult result) {
        var properties = obj.meta().properties();
        for (var property : properties) {
            if (obj.isPropertyDirty(property.name())) {
                // 我们只用验证脏属性
                validateProperty(obj, property, actionType, result);
            }

        }
    }

    private void validateProperty(IDomainObject obj, PropertyMeta property, ScheduledActionType actionType, ValidationResult result) {
        for (var validator : property.validators()) {
            validator.validate(obj, property, actionType, result);
        }
    }

    private void validateObject(IDomainObject obj, ScheduledActionType actionType, ValidationResult result) {
        for (var validator : obj.validators()) {
            validator.validate(obj, actionType, result);
        }
    }

    public static final FixedRules Instance = new FixedRules();
}
