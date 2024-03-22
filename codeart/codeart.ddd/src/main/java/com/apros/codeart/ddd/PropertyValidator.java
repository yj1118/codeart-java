package com.apros.codeart.ddd;

public abstract class PropertyValidator implements IPropertyValidator {

	public void validate(IDomainObject domainObject, IDomainProperty property, ValidationResult result) {
		var obj = (DomainObject) domainObject;
		var pro = (DomainProperty) property;
		var propertyValue = obj.getValue(pro);
		Validate(obj, pro, propertyValue, result);
	}

	protected abstract void Validate(DomainObject domainObject, DomainProperty property, Object propertyValue,
			ValidationResult result);
}