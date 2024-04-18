package apros.codeart.ddd;

public class PropertyError extends ValidationError {

	private String _propertyName;

	public String getPropertyName() {
		return _propertyName;
	}

	PropertyError(String code, String message, String propertyName) {
		super(code, message);
		_propertyName = propertyName;
	}
}
