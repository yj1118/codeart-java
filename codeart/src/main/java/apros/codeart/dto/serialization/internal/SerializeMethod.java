package apros.codeart.dto.serialization.internal;

import static apros.codeart.runtime.Util.propagate;

import java.lang.reflect.Method;

import apros.codeart.dto.IDTOWriter;

class SerializeMethod {

    private Method _method;

    public SerializeMethod(Method method) {
        _method = method;
    }

    public void invoke(Object instance, IDTOWriter writer) {
        try {
            _method.invoke(null, instance, writer);
        } catch (Throwable e) {
            throw propagate(e);
        }
    }
}
