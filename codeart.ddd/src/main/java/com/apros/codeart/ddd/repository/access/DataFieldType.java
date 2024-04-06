package com.apros.codeart.ddd.repository.access;

enum DataFieldType {
	Value, ValueObject, AggregateRoot, EntityObject, EntityObjectPro, ValueList, ValueObjectList, EntityObjectList,
	AggregateRootList,
	/// <summary>
	/// 由orm生成的键
	/// </summary>
	GeneratedField
}
