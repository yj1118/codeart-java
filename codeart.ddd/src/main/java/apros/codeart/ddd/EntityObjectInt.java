package apros.codeart.ddd;

import apros.codeart.ddd.repository.PropertyRepository;

@MergeDomain
@FrameworkDomain
public abstract class EntityObjectInt extends EntityObject {

	/// <summary>
	/// 引用对象的唯一标示
	/// 引用对象的标示可以是本地(内聚范围内)唯一也可以是全局唯一
	/// </summary>
	@PropertyRepository
	public static final DomainProperty IdProperty = DomainProperty.register(IdPropertyName, int.class,
			EntityObjectInt.class);

	public int id() {
		return this.getValue(IdProperty, int.class);
	}

	private void setId(int value) {
		this.setValue(IdProperty, value);
	}

	public EntityObjectInt(int id) {
		this.setId(id);
		this.onConstructed();
	}

	public Object getIdentity() {
		return this.id();
	}

}