package apros.codeart.ddd.repository.access;

public enum GeneratedFieldType {
	RootKey, MasterKey, SlaveKey,
	/// <summary>
	/// 值对象的主键
	/// </summary>
	ValueObjectPrimaryKey,
	/// <summary>
	/// 值集合的值字段
	/// </summary>
	PrimitiveValue,
	/// <summary>
	/// 标示数据被引用了多少次的键
	/// </summary>
	AssociatedCount,
	/// <summary>
	/// 中间表中用来存序号的键
	/// </summary>
	Index,
	/// <summary>
	/// 领域类型的编号
	/// </summary>
	TypeKey,
	/// <summary>
	/// 数据版本号
	/// </summary>
	DataVersion, User // 表示用户自定义的字段
}
