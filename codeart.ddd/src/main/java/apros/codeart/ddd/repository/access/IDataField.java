package apros.codeart.ddd.repository.access;

import apros.codeart.ddd.metadata.PropertyMeta;

public interface IDataField {

	/**
	 * 字段名称
	 * 
	 * @return
	 */
	String name();

	void name(String value);

	/**
	 * 
	 * 字段是否为附加的，这意味着不是从领域对象中分析出来的，而是通过数据映射器手工添加的字段,这类字段一般用于性能优化
	 * 这类字段不影响对象数据版本号，也不会在领域层中使用（所以不会出现在数据代理DataProxyPro的OriginalData中）
	 * 附加字段的维护由程序员通过自定义数据映射器负责
	 * 
	 * @return
	 */
	boolean isAdditional();

	void isAdditional(boolean value);

	/**
	 * 字段类型
	 * 
	 * @return
	 */
	DataFieldType fieldType();

	/**
	 * 
	 * 字段对应的数据库类型
	 * 
	 * @return
	 */
	DbType dbType();

	/**
	 * 是否为主键
	 * 
	 * @return
	 */
	boolean isPrimaryKey();

	/**
	 * 是否为聚集索引
	 * 
	 * @return
	 */
	boolean isClusteredIndex();

	/**
	 * 是否为非聚集索引
	 * 
	 * @return
	 */
	boolean isNonclusteredIndex();

	/**
	 * 对应的属性的元数据
	 * 
	 * @return
	 */
	PropertyMeta tip();

	String propertyName();

	Class<?> propertyType();

	Class<?> reflectedType();

	/**
	 * 
	 * 所属父成员字段，例如book.category.cover
	 * 对于字段cover(BookCover表)的MemberField字段就是cover，BookCover表的ParentMemberField就是category
	 * 
	 * @return
	 */
	IDataField parentMemberField();

	void parentMemberField(IDataField value);

	/**
	 * 字段所在的表
	 * 
	 * @return
	 */
	DataTable table();

	void table(DataTable value);

	/**
	 * 
	 * 由于memberField会先建立，然后创建对应的表，所以再这个期间，
	 * memberField的Table为null,因此我们会记录TableName,以便别的对象使用
	 * 
	 * @return
	 */
	String tableName();

	void tableName(String value);

	String masterTableName();

	void masterTableName(String value);

	/**
	 * 表示该字段指示的是否为多行数据（集合）
	 * 
	 * @return
	 */
	boolean isMultiple();
}
