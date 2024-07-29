package apros.codeart.dto.serialization.internal;

import java.lang.reflect.Field;

import apros.codeart.dto.serialization.DTOMember;

public class DTOMemberImpl {

    private String _name;

    public String getName() {
        return _name;
    }

    public DTOMemberImpl(String name) {
        _name = name;
    }

    public static DTOMemberImpl get(Field field) {

        var ann = field.getAnnotation(DTOMember.class);

        return ann != null ? new DTOMemberImpl(ann.value()) : null;
    }
}
