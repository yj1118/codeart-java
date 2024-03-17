package com.apros.codeart.ddd;

enum DomainPropertyType {
	/// <summary>
	/// 基元类型
	/// </summary>
	Primitive,
	/// <summary>
	/// 基元类型的集合
	/// </summary>
	PrimitiveList, ValueObject, AggregateRoot, EntityObject, ValueObjectList, EntityObjectList, AggregateRootList
}
