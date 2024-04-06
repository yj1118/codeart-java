package com.apros.codeart.ddd.repository.access;

enum DbFieldType {
	/// <summary>
	/// 主键
	/// </summary>
	PrimaryKey,
	/// <summary>
	/// 聚集索引
	/// </summary>
	ClusteredIndex,
	/// <summary>
	/// 非聚集索引
	/// </summary>
	NonclusteredIndex,
	/// <summary>
	/// 普通的键
	/// </summary>
	Common
}
