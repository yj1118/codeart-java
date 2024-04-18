package apros.codeart.ddd.repository.access;

public class IntField extends DbField {

	public Class<?> valueType() {
		return int.class;
	}

	public IntField(String name) {
		super(name);
	}
}
