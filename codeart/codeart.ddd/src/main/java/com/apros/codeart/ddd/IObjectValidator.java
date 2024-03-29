package com.apros.codeart.ddd;

public interface IObjectValidator {
	void validate(IDomainObject domainObject, ValidationResult result);
}
