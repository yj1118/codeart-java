package apros.codeart.ddd.virtual;

import apros.codeart.ddd.DomainCollection;
import apros.codeart.ddd.DomainProperty;

import java.io.Serial;

class VirtualCollection extends DomainCollection<Object> {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 2374369813830873290L;

    public VirtualCollection(DomainProperty propertyInParent) {
        super(Object.class, propertyInParent);
    }

}
