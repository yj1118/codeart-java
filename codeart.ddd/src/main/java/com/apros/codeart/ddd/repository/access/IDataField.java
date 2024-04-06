package com.apros.codeart.ddd.repository.access;

public interface IDataField {
	/// <summary>
	/// 字段名称
	/// </summary>
	string Name
	{
	    get;
	    set;
	}

	/// <summary>
	/// 字段是否为附加的，这意味着不是从领域对象中分析出来的，而是通过数据映射器手工添加的字段,这类字段一般用于性能优化
	/// 这类字段不影响对象数据版本号，也不会在领域层中使用（所以不会出现在数据代理DataProxyPro的OriginalData中）
	/// 附加字段的维护由程序员通过自定义数据映射器负责
	/// </summary>
	bool IsAdditional
	{
	    get;
	    set;
	}

	/// <summary>
	/// 字段类型
	/// </summary>
	DataFieldType FieldType
	{
	    get;
	}

	/// <summary>
	/// 字段对应的数据库类型
	/// </summary>
	DbType DbType
	{
	    get;
	}

	/// <summary>
	/// 是否为主键
	/// </summary>
	bool IsPrimaryKey
	{
	    get;
	}

	/// <summary>
	/// 是否为聚集索引
	/// </summary>
	bool IsClusteredIndex
	{
	    get;
	}

	/// <summary>
	/// 是否为非聚集索引
	/// </summary>
	bool IsNonclusteredIndex
	{
	    get;
	}

	/// <summary>
	/// 定义的仓储特性
	/// </summary>
	PropertyRepositoryAttribute Tip
	{
	    get;
	}

	/// <summary>
	/// 所属父成员字段，例如book.category.cover
	/// 对于字段cover(BookCover表)的MemberField字段就是cover，BookCover表的ParentMemberField就是category
	/// </summary>
	IDataField ParentMemberField
	{
	    get;
	    set;
	}

	/// <summary>
	/// 字段所在的表
	/// </summary>
	DataTable Table
	{
	    get;
	    set;
	}

	/// <summary>
	/// 由于memberField会先建立，然后创建对应的表，所以再这个期间，
	/// memberField的Table为null,因此我们会记录TableName,以便别的对象使用
	/// </summary>
	string TableName
	{
	    get;
	    set;
	}

	string MasterTableName
	{
	    get;
	    set;
	}

	/// <summary>
	/// 表示该字段指示的是否为多行数据（集合）
	/// </summary>
	bool IsMultiple
	{
	    get;
	}
}
