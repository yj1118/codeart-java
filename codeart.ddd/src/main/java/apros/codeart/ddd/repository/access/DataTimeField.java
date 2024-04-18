package apros.codeart.ddd.repository.access;

import java.time.LocalDateTime;

public class DataTimeField extends DbField {

	public Class<?> valueType() {
		return LocalDateTime.class;
	}

	public DataTimeField(String name) {
		super(name);
	}
}
