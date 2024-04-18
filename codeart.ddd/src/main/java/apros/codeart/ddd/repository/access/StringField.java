package apros.codeart.ddd.repository.access;

public class StringField extends DbField {
	private int _maxLength;

	public int maxLength() {
		return _maxLength;
	}

	private boolean _ascii;

	public boolean ascii() {
		return _ascii;
	}

	public Class<?> valueType() {
		return String.class;
	}

	public StringField(String name, int maxLength, boolean ascii) {
		super(name);
		_maxLength = maxLength;
		_ascii = ascii;
	}
}