package apros.codeart.bytecode;

public class StackItem {

    private final ClassWrapper _valueType;

    public ClassWrapper getValueType() {
        return _valueType;
    }

    public boolean isPrimitive() {
        return _valueType.isPrimitive();
    }

    public boolean isRef() {
        return !this.isPrimitive();
    }

    public StackItem(Class<?> valueType) {
        _valueType = new ClassWrapper(valueType);
    }

    public StackItem(String valueTypeName) {
        _valueType = new ClassWrapper(valueTypeName);
    }


}
