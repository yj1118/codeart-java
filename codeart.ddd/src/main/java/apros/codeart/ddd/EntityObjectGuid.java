package apros.codeart.ddd;

import java.util.UUID;

import apros.codeart.ddd.repository.PropertyRepository;

@MergeDomain
@FrameworkDomain
public abstract class EntityObjectGuid extends EntityObject {

	@PropertyRepository
	public static final DomainProperty IdProperty = DomainProperty.register(IdPropertyName, UUID.class,
			EntityObjectGuid.class);

	public UUID id() {
		return this.getValue(IdProperty, UUID.class);
	}

	private void setId(UUID value) {
		this.setValue(IdProperty, value);
	}

	public EntityObjectGuid(UUID id) {
		this.setId(id);
		this.onConstructed();
	}

	public Object getIdentity() {
		return this.id();
	}

}