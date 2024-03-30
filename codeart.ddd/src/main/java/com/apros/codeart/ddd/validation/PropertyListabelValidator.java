package com.apros.codeart.ddd.validation;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.DomainProperty;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.DomainProperty;
import com.apros.codeart.ddd.IPropertyValidator;
import com.apros.codeart.ddd.ValidationResult;
import com.apros.codeart.runtime.TypeUtil;

/**
 * 既能验证目标类型，也能验证当目标类型为集合的成员类型时的情况
 */
public abstract class PropertyListabelValidator implements IPropertyValidator {

	public void validate(IDomainObject domainObject, DomainProperty property, ValidationResult result) {
		var obj = (DomainObject) domainObject;
		var pro = (DomainProperty) property;

		if (TypeUtil.isCollection(pro.declaringType())) {
			var values = TypeUtil.as(obj.getValue(pro), Iterable.class);
			for (var value : values) {
				validate(obj, pro, value, result);
			}
		} else {
			var propertyValue = obj.getValue(pro);
			validate(obj, pro, propertyValue, result);
		}
	}

	protected abstract void validate(DomainObject domainObject, DomainProperty property, Object propertyValue,
			ValidationResult result);
}
