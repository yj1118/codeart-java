package apros.codeart.ddd.metadata.internal;

import apros.codeart.ddd.PropertyValidatorImpl;
import apros.codeart.ddd.metadata.MetadataScheme;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.dto.DTObject;

public final class SchemeCodeReader {
    private SchemeCodeReader() {
    }

    /**
     * 目前仅读取了 {@meta} 的关于持久化方面所必须的信息，以后可以再包含更多的内容
     *
     * @param <T>
     * @param meta
     * @return
     */
    public static DTObject read(ObjectMeta meta, Iterable<String> propertyNames) {

        var scheme = new MetadataScheme(meta.name(), meta.category());

        for (var propertyName : propertyNames) {
            var tip = meta.findProperty(propertyName);
            addProperty(scheme, tip);
        }

        return scheme.getData();
    }

    private static void addProperty(MetadataScheme scheme, PropertyMeta tip) {

        scheme.addProperty(tip.name(), tip.category(), tip.monotype().getSimpleName(), tip.lazy(), (property) -> {
            for (var validator : tip.validators()) {
                var annTypeName = PropertyValidatorImpl.getAnnotationName(validator);
                var valData = validator.getData();
                property.addValidator(annTypeName, valData);
            }
        });

    }

}
