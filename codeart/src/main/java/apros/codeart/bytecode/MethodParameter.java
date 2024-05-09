package apros.codeart.bytecode;

public class MethodParameter {
	private String _name;

	public String getName() {
		return _name;
	}

	private Class<?> _type;

	public Class<?> getType() {
		return _type;
	}

	public MethodParameter(String name, Class<?> type) {
		_name = name;
		_type = type;
	}

}
