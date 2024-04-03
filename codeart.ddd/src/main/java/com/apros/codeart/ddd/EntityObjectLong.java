package com.apros.codeart.ddd;

import com.apros.codeart.ddd.repository.PropertyRepository;

@MergeDomain
@FrameworkDomain
public abstract class EntityObjectLong extends EntityObject {

	/// <summary>
	/// 引用对象的唯一标示
	/// 引用对象的标示可以是本地(内聚范围内)唯一也可以是全局唯一
	/// </summary>
	@PropertyRepository
	public static final DomainProperty IdProperty = DomainProperty.register(IdPropertyName, long.class,
			EntityObjectLong.class);

	public long id() {
		return this.getValue(IdProperty, long.class);
	}

	private void setId(long value) {
		this.setValue(IdProperty, value);
	}

	public EntityObjectLong(long id) {
		this.setId(id);
		this.onConstructed();
	}

	public Object getIdentity() {
		return this.id();
	}

}