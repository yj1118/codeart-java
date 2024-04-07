package com.apros.codeart.ddd.repository.access;

public enum DataFieldType {
	Value, ValueObject, AggregateRoot, EntityObject, ValueList, ValueObjectList, EntityObjectList, AggregateRootList,
	/// <summary>
	/// 由orm生成的键
	/// </summary>
	GeneratedField
}
