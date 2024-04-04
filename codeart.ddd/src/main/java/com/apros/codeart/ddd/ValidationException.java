package com.apros.codeart.ddd;

import com.apros.codeart.UserUIException;

public class ValidationException extends UserUIException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7233981740857887941L;
	private ValidationResult _result;

	public ValidationResult result() {
		return _result;
	}

	public ValidationException(ValidationResult result) {
		super(result.getMessage());
		_result = result;
	}
}
