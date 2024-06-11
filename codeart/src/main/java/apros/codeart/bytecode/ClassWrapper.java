package apros.codeart.bytecode;

import apros.codeart.runtime.TypeUtil;
import org.objectweb.asm.Opcodes;

import java.util.Objects;

public class ClassWrapper {

    private final Class<?> _type;

    public Class<?> type() {
        return _type;
    }

    private final String _name;

    public String name() {
        return _name;
    }

    private ClassWrapper(Class<?> type, String name) {
        _type = type;
        _name = name;
    }

    public ClassWrapper(Class<?> type) {
        this(type, null);
    }

    public ClassWrapper(String name) {
        this(null, name);
    }

    public boolean isVoid() {
        if (_type != null) return _type == void.class;
        if (_name != null) return _name.equals("void");
        return false;
    }

    public boolean isPrimitive() {
        if (_type != null) return _type.isPrimitive();
        return false;
    }

    public boolean isInt() {
        if (_type != null) return _type == int.class;
        return Objects.equals(_name, "int");
    }

    public boolean isLong() {
        if (_type != null) return _type == long.class;
        return Objects.equals(_name, "long");
    }

    public boolean isFloat() {
        if (_type != null) return _type == float.class;
        return Objects.equals(_name, "float");
    }

    public boolean isDouble() {
        if (_type != null) return _type == double.class;
        return Objects.equals(_name, "double");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        ClassWrapper that = TypeUtil.as(o, ClassWrapper.class);
        if (that == null) return false;

        if (this._type != null) {
            return _type == that._type;
        }

        return Objects.equals(_name, that._name);
    }

    @Override
    public int hashCode() {
        if (_type != null) return _type.hashCode();
        if (_name != null) return _name.hashCode();
        return 0;
    }
}
