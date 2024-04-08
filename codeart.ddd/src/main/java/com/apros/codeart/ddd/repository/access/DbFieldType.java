package com.apros.codeart.ddd.repository.access;

public enum DbFieldType {
	/// <summary>
	/// 主键
	/// </summary>
	PrimaryKey,
	/// <summary>
	/// 聚集索引
	/// </summary>
	ClusteredIndex,

	/**
	 * 非聚集索引
	 */
	NonclusteredIndex,

	/**
	 * 普通的键
	 */
	Common
}
