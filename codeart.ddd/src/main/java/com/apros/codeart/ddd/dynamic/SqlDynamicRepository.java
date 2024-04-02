package com.apros.codeart.ddd.dynamic;

import com.apros.codeart.ddd.IRepository;

public abstract class SqlDynamicRepository implements IRepository {

	private Class<?> _objectType;

	public Class<?> ObjectType() {
		return _objectType;
	}

	public SqlDynamicRepository(Class<?> objectType) {
		_objectType = objectType;
	}

}
