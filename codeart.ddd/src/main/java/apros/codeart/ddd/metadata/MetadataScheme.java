package apros.codeart.ddd.metadata;

import apros.codeart.ddd.PropertyValidatorImpl;
import apros.codeart.dto.DTObject;

import java.util.function.Consumer;

public class MetadataScheme {

    private final DTObject _data;

    public MetadataScheme(String className, DomainObjectCategory category) {
        _data = DTObject.editable();
        _data.setString("name", className);
        _data.setByte("category", category.value());
    }

    public void addProperty(String propertyName,
                            DomainPropertyCategory category,
                            String monotypeTypeName,
                            boolean lazy, Consumer<MetadataPropertyScheme> set) {

        var ps = new MetadataPropertyScheme(propertyName, monotypeTypeName, category, lazy);
        set.accept(ps);
        _data.push("props", ps.getData());
    }

    public DTObject getData() {
        return _data;
    }

}
