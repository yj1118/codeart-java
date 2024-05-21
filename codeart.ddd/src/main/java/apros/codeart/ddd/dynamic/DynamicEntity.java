package apros.codeart.ddd.dynamic;

import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.FrameworkDomain;
import apros.codeart.ddd.IEntityObject;
import apros.codeart.ddd.MergeDomain;
import apros.codeart.ddd.repository.ConstructorRepository;

@MergeDomain
@FrameworkDomain
public class DynamicEntity extends DynamicObject implements IEntityObject {

	public DynamicEntity(boolean isEmpty) {
		super(isEmpty);
		this.onConstructed();
	}

	@ConstructorRepository
	public DynamicEntity() {
		super();
		this.onConstructed();
	}

	public Object getIdentity() {
		var property = DomainProperty.getProperty(this.getClass(), EntityObject.IdPropertyName);
		return this.getValue(property);
	}

}
