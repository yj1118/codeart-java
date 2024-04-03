package com.apros.codeart.ddd;

/**
 * 状态事件的类型
 */
public enum StatusEventType {
	/// <summary>
	/// 构造领域对象之后
	/// </summary>
	Constructed,

	/// <summary>
	/// 领域对象被改变后
	/// </summary>
	Changed,

	/// <summary>
	/// 提交新增操作到仓储之前
	/// </summary>
	PreAdd,

	/// <summary>
	/// 提交新增操作到仓储之后，
	/// 注意，该事件只是领域对象被提交要保存，而不是真的已保存，有可能由于工作单元
	/// 此时还未真正保存到仓储，要捕获真是保存到仓储之后的事件请用Committed版本
	/// </summary>
	Added,

	/// <summary>
	/// 提交修改操作到仓储之前
	/// </summary>
	PreUpdate,
	/// <summary>
	/// 提交修改操作到仓储之后
	/// 注意，该事件只是领域对象被提交要保存，而不是真的已保存，有可能由于工作单元
	/// 此时还未真正保存到仓储，要捕获真是保存到仓储之后的事件请用Committed版本
	/// </summary>
	Updated,

	/// <summary>
	/// 提交删除操作到仓储之前
	/// </summary>
	PreDelete,
	/// <summary>
	/// 提交删除操作到仓储之后
	/// 注意，该事件只是领域对象被提交要保存，而不是真的已保存，有可能由于工作单元
	/// 此时还未真正保存到仓储，要捕获真是保存到仓储之后的事件请用Committed版本
	/// </summary>
	Deleted,

	/// <summary>
	/// 对象被真实提交的前一刻
	/// </summary>
	AddPreCommit,

	/// <summary>
	/// 对象被真实提交到仓储保存后
	/// </summary>
	AddCommitted,

	/// <summary>
	/// 对象被真实提交到仓储修改的前一刻
	/// </summary>
	UpdatePreCommit,

	/// <summary>
	/// 对象被真实提交到仓储修改后
	/// </summary>
	UpdateCommitted,

	/// <summary>
	/// 对象被真实提交到仓储删除后
	/// </summary>
	DeletePreCommit,

	/// <summary>
	/// 对象被真实提交到仓储删除后
	/// </summary>
	DeleteCommitted,

	/// <summary>
	/// 通用的状态事件
	/// </summary>
	Any,
}
