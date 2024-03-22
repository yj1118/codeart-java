package com.apros.codeart.ddd;

public interface IPropertyValidator {
	void validate(IDomainObject domainObject, IDomainProperty property, ValidationResult result);
}
