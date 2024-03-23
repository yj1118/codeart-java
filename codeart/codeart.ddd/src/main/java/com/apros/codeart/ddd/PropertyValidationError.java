package com.apros.codeart.ddd;

public class PropertyValidationError extends ValidationError {

	private String _propertyName;

	public String getPropertyName() {
		return _propertyName;
	}

	PropertyValidationError(String code, String message, String propertyName) {
		super(code, message);
		_propertyName = propertyName;
	}
}
