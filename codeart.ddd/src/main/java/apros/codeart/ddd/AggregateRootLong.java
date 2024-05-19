package apros.codeart.ddd;

import apros.codeart.ddd.repository.PropertyRepository;

public abstract class AggregateRootLong extends AggregateRoot {

	@PropertyRepository
	public static final DomainProperty IdProperty = DomainProperty.register(IdPropertyName, long.class,
			AggregateRootLong.class);

	public long id() {
		return this.getValue(IdProperty, long.class);
	}

	private void setId(long value) {
		this.setValue(IdProperty, value);
	}

	public AggregateRootLong(long id) {
		this.setId(id);
		this.onConstructed();
	}

	public Object getIdentity() {
		return this.id();
	}
}
