package apros.codeart.ddd.virtual;

import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.FrameworkDomain;
import apros.codeart.ddd.IEntityObject;
import apros.codeart.ddd.MergeDomain;
import apros.codeart.ddd.repository.ConstructorRepository;

@MergeDomain
@FrameworkDomain
public class VirtualEntity extends VirtualObject implements IEntityObject {

    public VirtualEntity(boolean isEmpty) {
        super(isEmpty);
        this.onConstructed();
    }

    @ConstructorRepository
    public VirtualEntity() {
        super();
        this.onConstructed();
    }

    public Object getIdentity() {
        return this.getValue(EntityObject.IdPropertyName);
    }

}
