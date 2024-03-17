package com.apros.codeart.ddd;

import com.apros.codeart.util.StringUtil;

public class PropertyError extends ValidationError {

	private String _propertyName;

	public String getPropertyName() {
		return _propertyName;
	}

	void setPropertyName(String propertyName) {
		_propertyName = propertyName;
	}

	PropertyError() {
	}

	@Override
	public void Clear() {
		super.Clear();
		_propertyName = StringUtil.empty();
	}

	private static PropertyError CreateReusable(String propertyName, String code, String message) {
		return null;
//        var error = Session.TryMark(_propertyErrorPool, () =>
//        {
//            return new PropertyValidationError();
//        });
//        error.PropertyName = propertyName;
//        error.ErrorCode = errorCode;
//        error.Message = message;
//        return error;
	}

}
