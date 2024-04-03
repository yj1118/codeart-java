package com.apros.codeart.ddd.dynamic;

import com.apros.codeart.ddd.ConstructorRepository;
import com.apros.codeart.ddd.DomainProperty;
import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.IEntityObject;
import com.apros.codeart.dto.DTObject;
import com.apros.codeart.dto.serialization.IDTOSerializable;

public class DynamicEntity extends DynamicObject implements IEntityObject, IDTOSerializable {

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

	@Override
	public void serialize(DTObject owner, String name) {
		if (this.isEmpty())
			return;
		owner.setValue(name, this.getData());
	}
}
