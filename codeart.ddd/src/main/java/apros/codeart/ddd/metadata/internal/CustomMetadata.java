package apros.codeart.ddd.metadata.internal;

import apros.codeart.App;
import apros.codeart.ddd.metadata.IMetadataSchemeFactory;
import apros.codeart.ddd.metadata.SchemeCode;
import apros.codeart.runtime.Activator;

public final class CustomMetadata {
    private CustomMetadata() {
    }

    public static void initialize() {
        var factoryTypes = Activator.getSubTypesOf(IMetadataSchemeFactory.class, App.archives());
        for (var factoryType : factoryTypes) {
            var factory = (IMetadataSchemeFactory) Activator.createInstance(factoryType);
            var schemes = factory.getSchemes();
            for (var scheme : schemes) {
                var dynamicType = SchemeCode.parse(scheme);
                MetadataLoader.register(dynamicType);
                System.out.println("Loaded scheme object meta: " + dynamicType.getSimpleName());
            }
        }
    }
}