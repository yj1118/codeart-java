package com.apros.codeart.ddd;

import java.util.ArrayList;

import com.apros.codeart.util.StringUtil;

public class ValidationResult {

	private ArrayList<ValidationError> _errors;

	private ValidationResult() {
		_errors = new ArrayList<ValidationError>();
	}

	public int getErrorCount() {
		return _errors.size();
	}

	public boolean isSatisfied() {
		return _errors.size() == 0;
	}

//	public void addError(IDomainProperty property, String code, String message) {
//		var error = createPropertyError(property.getName(), code, message);
//		_errors.add(error);
//	}
//
//	/// <summary>
//	/// 向验证结果中添加一个错误
//	/// </summary>
//	public void addError(String propertyName, String errorCode, String message) {
//		var error = createPropertyError(propertyName, errorCode, message);
//		_errors.add(error);
//	}
//
//	public void AddError(String message) {
//		var error = createError(string.Empty, message);
//		_errors.Add(error);
//	}
//
//	public void AddError(String errorCode, String message) {
//		var error = createError(errorCode, message);
//		_errors.add(error);
//	}

	public String getMessage() {
		if (this.isSatisfied())
			return "success";
		else {
			return StringUtil.lines(_errors, (error) -> error.getMessage());
		}
	}

}
