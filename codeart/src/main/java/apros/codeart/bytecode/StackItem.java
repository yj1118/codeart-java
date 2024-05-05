package apros.codeart.bytecode;

public class StackItem {

	private Class<?> _valueType;

	public Class<?> getValueType() {
		return _valueType;
	}

	public boolean isPrimitive() {
		return _valueType.isPrimitive();
	}

	public boolean isRef() {
		return !_valueType.isPrimitive();
	}

	public StackItem(Class<?> valueType) {
		_valueType = valueType;
	}

}
