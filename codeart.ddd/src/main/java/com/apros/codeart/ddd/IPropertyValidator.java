package com.apros.codeart.ddd;

public interface IPropertyValidator {
	void Validate(IDomainObject domainObject, IDomainProperty property, ValidationResult result);
}
