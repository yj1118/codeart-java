package apros.codeart.ddd.repository.access;

public class DoubleField extends DbField {

	public Class<?> valueType() {
		return boolean.class;
	}

	public DoubleField(String name) {
		super(name);
	}
}
