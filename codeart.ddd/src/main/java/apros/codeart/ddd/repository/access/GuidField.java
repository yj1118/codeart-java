package apros.codeart.ddd.repository.access;

import java.util.UUID;

public class GuidField extends DbField {

	public Class<?> valueType() {
		return UUID.class;
	}

	public GuidField(String name) {
		super(name);
	}
}