package apros.codeart.ddd;

import apros.codeart.util.StringUtil;

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

	private static PropertyError CreateReusable(String propertyName, String code, String message)
    {
        var error = Symbiosis.TryMark(_propertyErrorPool, () =>
        {
            return new PropertyValidationError();
        });
        error.PropertyName = propertyName;
        error.ErrorCode = errorCode;
        error.Message = message;
        return error;
    }

}
