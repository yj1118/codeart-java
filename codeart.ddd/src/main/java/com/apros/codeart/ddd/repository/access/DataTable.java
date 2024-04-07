package com.apros.codeart.ddd.repository.access;

public class DataTable {

	private String _id;

	/**
	 * 
	 * 表的唯一表号，由引用路径生成
	 * 
	 * @return
	 */
	public String id() {
		return _id;
	}

	private Class<?> _objectType;

	/**
	 * 
	 * 表对应对实体类型，注意中间表对应的是集合类型
	 * 
	 * @return
	 */
	public Class<?> objectType() {
		return _objectType;
	}

	private DataTableType _type;

	/**
	 * 
	 * 表类型
	 * 
	 * @return
	 */
	public DataTableType type() {
		return _type;
	}

	public String _name;

	/**
	 * 表名称
	 * 
	 * @return
	 */
	public String name() {
		return _name;
	}

	private DataTable _chainRoot;

	/**
	 * 
	 * 在引用链中的根表，这个属性大多数情况等属性Root，但是在跨根引用中会有所不同
	 * 
	 * 例如：根 role 引用了根 organization,而根 organization 又有属性 permissions 引用了多个根permission
	 * 
	 * 这时候对于permission表，Root是organization,ChainRoot是role
	 * 
	 * @return
	 */
	public DataTable chainRoot() {
		return _chainRoot;
	}

	private DataTable _root;

	/**
	 * 所属根表（领域根）
	 * 
	 * @return
	 */
	public DataTable root() {
		return _root;
	}

	private DataTable _master;

	/**
	 * 
	 * 所属主表
	 * 
	 * @return
	 */
	public DataTable master() {
		return _master;
	}

	private IDataField _memberField;

	/**
	 * 
	 * 该表在对象中所在成员的字段定义
	 * 
	 * @return
	 */
	public IDataField memberField() {
		return _memberField;
	}

	private Class<?> _elementType;

	/**
	 * 
	 * 该值仅在中间表时才有用
	 * 
	 * @return
	 */
	public Class<?> elementType() {
		return _elementType;
	}

	private ObjectChain _chain;

	/**
	 * 
	 * 该表的引用链
	 * 
	 * @return
	 */
	public ObjectChain chain() {
		return _chain;
	}

	void setChain(ObjectChain chain) {
		_chain = chain;
	}

	static DataTable createRoot(Class<?> objectType, DataTableType type, String name,
			Iterable<IDataField> objectFields) {
		return new DataTable();
	}

	DataTable(Class<?> objectType, DataTableType type, String name, DataTable chainRoot, DataTable master,
			Iterable<IDataField> tableFields, Iterable<IDataField> objectFields, IDataField memberField) {
		_id = DataTableUtil.getId(memberField, chainRoot, name);
		_objectType = objectType;
		_type = type;
		_name = name;
		
		 if(memberField != null) memberField.table(this);
		 
		 this._chainRoot = chainRoot;
		 
		 this._master = master;
		 
		 

		 this.MemberField = memberField;
		 this.Root = FindActualRoot(chainRoot);
		 InitObjectType(objectType, memberField?.Tip);

		 this.Chain = this.MemberField == null ? ObjectChain.Empty : new ObjectChain(this.MemberField);
		 this.IsSnapshot = isSnapshot;

		 this.IsMultiple = memberField == null ? false : memberField.IsMultiple;

		 this.Name = name;
		 this.Fields = TidyFields(tableFields);

		 this.ObjectFields = objectFields;
		 this.PropertyTips = GetPropertyTips();
		 InitDerived();
		 //InitConnectionName();
		 InitTableIdName();
		 InitChilds();
		 InitDynamic();
		 this.Mapper = DataMapperFactory.Create(this.ObjectType);
		 //这里触发，是为了防止程序员在程序启动时手工初始化，但会遗漏动态表的初始化
		 //所以在表构造的时候主动创建
		 this.Build();
	}

}
