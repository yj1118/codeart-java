package apros.codeart.ddd.metadata.internal;

import apros.codeart.App;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.cqrs.CQRSConfig;
import apros.codeart.ddd.metadata.IMetadataSchemeFactory;
import apros.codeart.ddd.metadata.SchemeCode;
import apros.codeart.runtime.Activator;
import apros.codeart.util.SafeAccessImpl;

import java.util.ArrayList;

public final class MetadataSchemeFactory {
    private MetadataSchemeFactory() {
    }

    public static void initialize() {
        var factoryTypes = Activator.getSubTypesOf(IMetadataSchemeFactory.class, App.archives());
        for (var factoryType : factoryTypes) {
            var factory = (IMetadataSchemeFactory) Activator.createInstance(factoryType);
            var schemes = factory.getSchemes();
            for (var scheme : schemes) {
                var dynamicType = SchemeCode.parse(scheme);
                MetadataLoader.register(dynamicType);
                System.out.println("Loaded object meta: " + dynamicType.getSimpleName());
            }
        }
    }
}