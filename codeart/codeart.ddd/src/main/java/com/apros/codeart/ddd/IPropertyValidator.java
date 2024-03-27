package com.apros.codeart.ddd;

public interface IPropertyValidator {
	void validate(IDomainObject domainObject, DomainProperty property, ValidationResult result);
}
