package apros.codeart.ddd.repository.access;

public class FloatField extends DbField {

	public Class<?> valueType() {
		return float.class;
	}

	public FloatField(String name) {
		super(name);
	}
}
