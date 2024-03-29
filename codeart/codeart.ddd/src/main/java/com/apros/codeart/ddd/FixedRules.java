package com.apros.codeart.ddd;

import com.apros.codeart.util.SafeAccess;

@SafeAccess
public class FixedRules implements IFixedRules {

	private FixedRules() {
	}

	public ValidationResult validate(IDomainObject obj) {
		ValidationResult result = ValidationResult.create();
		validateProperties(obj, result); // 先验证属性
		validateObject(obj, result); // 再验证对象
		return result;
	}

//	#region 验证属性

	private void validateProperties(IDomainObject obj, ValidationResult result) {
		var properties = DomainProperty.getProperties(obj.getClass());
		for (var property : properties) {
			if (obj.isPropertyDirty(property)) {
				// 我们只用验证脏属性
				validateProperty(obj, property, result);
			}

		}
	}

	private void validateProperty(IDomainObject obj, DomainProperty property, ValidationResult result) {
		for (var validator : property.validators()) {
			validator.validate(obj, property, result);
		}
	}

	private void validateObject(IDomainObject obj, ValidationResult result) {
		for (var validator : obj.validators()) {
			validator.validate(obj, result);
		}
	}

	public static final FixedRules Instance = new FixedRules();
}
