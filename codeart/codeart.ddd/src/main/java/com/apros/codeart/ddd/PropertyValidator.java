package com.apros.codeart.ddd;

public abstract class PropertyValidator implements IPropertyValidator {

	public void validate(IDomainObject domainObject, DomainProperty property, ValidationResult result) {
		var obj = (DomainObject) domainObject;
		var pro = (DomainProperty) property;
		var propertyValue = obj.getValue(pro);
		validate(obj, pro, propertyValue, result);
	}

	protected abstract void validate(DomainObject domainObject, DomainProperty property, Object propertyValue,
			ValidationResult result);
}