package com.apros.codeart.ddd.repository.access;

import java.util.ArrayList;
import java.util.function.Function;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.metadata.ObjectMeta;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.ddd.metadata.ObjectRepositoryTip;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.ListUtil;

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

	private ObjectRepositoryTip _repositoryTip;

	public ObjectRepositoryTip repositoryTip() {
		return _repositoryTip;
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

	/**
	 * 该表是否为根表
	 * 
	 * @return
	 */
	public boolean isAggregateRoot() {
		// 有可能对象标记的ObjectTip的类型与对象的实际类型不同，所以需要使用ObjectType
		return this.objectType() == null ? false : ObjectMeta.isAggregateRoot(this.objectType());
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

	private DataTable _middle;

	/**
	 * 
	 * 与该表相关的中间表
	 * 
	 * @return
	 */
	public DataTable middle() {
		return _middle;
	}

	void middle(DataTable value) {
		_middle = value;
	}

	private DataTable _slave;

	/**
	 * 
	 * 该表相关的从表，该属性在middle表中存在
	 * 
	 * @return
	 */
	public DataTable slave() {
		return _slave;
	}

	void slave(DataTable value) {
		_slave = value;
	}

	private IDataField _memberField;

	private void initMemberField(String tableName, DataTable master, IDataField memberField) {
		// 补全memberField信息
		if (memberField != null) {
			if (memberField.parentMemberField() == null && master != null)
				memberField.parentMemberField(master.memberField());

			if (memberField.masterTableName() == null && master != null)
				memberField.masterTableName(master.name());

			if (memberField.tableName() == null)
				memberField.tableName(tableName);

			memberField.table(this);
		}

		_memberField = memberField;
	}

	/**
	 * 
	 * 该表在对象中所在成员的字段定义
	 * 
	 * @return
	 */
	public IDataField memberField() {
		return _memberField;
	}

	public PropertyMeta memberPropertyTip() {
		if (this.memberField() == null)
			return null;
		return this.memberField().tip();
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

	/**
	 * 获取表相对于 {@code parent} 的属性路径下的对象链，格式是 a_b_c
	 * 
	 * @param parent
	 * @return
	 */
	public String getChainPath(DataTable parent) {
		return _chain.getPath(parent);
	}

	private Iterable<IDataField> _fields;

	public Iterable<IDataField> fields() {
		return _fields;
	}

	private Iterable<IDataField> getFields(Iterable<IDataField> objectFields) {
		var fields = DataTableUtil.getTableFields(objectFields);

		// 我们要保证rootId在第一列，Id在第二列
		// 这样不仅符合人们的操作习惯，在建立索引时，也会以rootId作为第一位，提高查询性能
		if (_type == DataTableType.AggregateRoot || _type == DataTableType.Middle)
			return fields;

		if (_type == DataTableType.ValueObject || _type == DataTableType.EntityObject) {
			// 需要补充根键，因为我们只有在内部得到了实际的root后才能算出根键
			// 根编号要放在最前面，方便优化索引性能
			var rootKey = DataTableUtil.getForeignKey(this.root(), GeneratedFieldType.RootKey, DbFieldType.PrimaryKey);
			var temp = new ArrayList<IDataField>();
			temp.add(rootKey);
			ListUtil.addRange(temp, fields);
			return temp;
		}

		return fields;
	}

	private Iterable<IDataField> _primaryKeys;

	public Iterable<IDataField> primaryKeys() {
		if (_primaryKeys == null)
			_primaryKeys = ListUtil.filter(this.fields(), (t) -> t.isPrimaryKey());
		return _primaryKeys;
	}

	private Iterable<IDataField> _clusteredIndexs;

	public Iterable<IDataField> clusteredIndexs() {
		if (_clusteredIndexs == null)
			_clusteredIndexs = ListUtil.filter(this.fields(), (t) -> t.isClusteredIndex());
		return _clusteredIndexs;
	}

	private Iterable<IDataField> _nonclusteredIndexs;

	public Iterable<IDataField> nonclusteredIndexs() {
		if (_nonclusteredIndexs == null)
			_nonclusteredIndexs = ListUtil.filter(this.fields(), (t) -> t.isNonclusteredIndex());
		return _nonclusteredIndexs;
	}

	private Iterable<IDataField> _objectFields;

	public Iterable<IDataField> objectFields() {
		return _objectFields;
	}

	private DataTable findActualRoot(DataTable root) {
		if (_type == DataTableType.AggregateRoot && this.memberField() == null)
			return this;
		var master = this.master();
		while (master != null) {
			if (master.type() == DataTableType.AggregateRoot)
				return master;
			master = master.master();
		}
		return null;
	}

	static DataTable createRoot(Class<?> objectType, DataTableType type, String name,
			Iterable<IDataField> objectFields) {
		return new DataTable(objectType, type, name, objectFields, null, null, null);
	}

	private void initObjectType(Class<?> objectType, PropertyMeta tip) {
		_objectType = objectType;
		if (_type == DataTableType.Middle) {
			_elementType = tip.monotype();
		} else if (_type == DataTableType.AggregateRoot) {
			var meta = ObjectMetaLoader.get(objectType);
			_repositoryTip = meta.repositoryTip();
		} else {
			var meta = ObjectMetaLoader.get(objectType);
			// 对于值对象和引用对象，如果没有定义ObjectRepositoryAttribute，那么使用根的ObjectRepositoryAttribute
			if (meta == null || meta.repositoryTip() == null) {
				_repositoryTip = this.root().repositoryTip();
			} else {
				_repositoryTip = meta.repositoryTip();
			}
		}
	}

	private IDataField _idField;

	public IDataField idField() {
		if (_idField == null)
			_idField = ListUtil.find(this.fields(), (t) -> t.name().equalsIgnoreCase(EntityObject.IdPropertyName));
		return _idField;
	}

	private boolean _isMultiple;

	/**
	 * 每次插入数据，是多条的
	 * 
	 * @return
	 */
	public boolean isMultiple() {
		return _isMultiple;
	}

	private String _tableIdName;

	/**
	 * 
	 * 格式为TableId的编号，例如：Book表的TableIdName就为 BookId
	 * 
	 * @return
	 */
	public String tableIdName() {
		return _tableIdName;
	}

	private void initTableIdName() {
		if (this.type() != DataTableType.Middle) {
			var type = this.objectType();
			this._tableIdName = String.format("%s%s", type.getSimpleName(), EntityObject.IdPropertyName);
		}
	}

	private ArrayList<DataTable> _buildtimeChilds;

	/**
	 * 构建时确定的表集合，该集合会运用查询
	 * 
	 * 使用该属性时注意由于对象关系的循环引用导致的死循环问题
	 * 
	 * @return
	 */
	public Iterable<DataTable> buildtimeChilds() {
		return _buildtimeChilds;
	}

	private void initChilds() {
		initGetRuntimeTable();
		initBuildtimeChilds();
	}

	/**
	 * 初始化构建时的表
	 * 
	 */
	private void initBuildtimeChilds() {
		_buildtimeChilds = new ArrayList<DataTable>();

		// 初始化相关的基础表
		for (var field : this.objectFields()) {
			var type = field.propertyType();
			if (TypeUtil.isCollection(type))
				type = field.tip().monotype();
			if (field.fieldType() == DataFieldType.GeneratedField || field.fieldType() == DataFieldType.Value)
				continue;

			var table = createChildTable(this, field, type);
			_buildtimeChilds.add(table);
		}
	}

	/**
	 * 
	 * 在运行时查找子表信息
	 * 
	 * @param master
	 * @param tip
	 * @return
	 */
	public DataTable findChild(DataTable master, PropertyMeta tip) {
		return _getRuntimeTable.apply(master).apply(tip.name()).apply(tip.monotype());
	}

	/**
	 * 根据属性名称，实际类型，获取对应的表信息
	 * 
	 * runtime表包括预定义的类型和运行时加载的类型对应的表
	 * 
	 * 本来通过master和master下的属性名称PropertyName，我们就可以找到子表的定义
	 * 
	 * 但是由于属性的值有可能是不同的类型（因为继承关系），所以我们还需要指定属性的类型objectType
	 * 
	 * 才能获得当前属性对应的表信息
	 * 
	 * @param master
	 * @param propertyName
	 * @param objectType   表示实际运行时propertyName对应的objectType
	 * @return
	 */
	public DataTable findChild(DataTable master, String propertyName, Class<?> objectType) {
		return _getRuntimeTable.apply(master).apply(propertyName).apply(objectType);
	}

	private Function<DataTable, Function<String, Function<Class<?>, DataTable>>> _getRuntimeTable;

	private void initGetRuntimeTable() {
		_getRuntimeTable = LazyIndexer.init((master) -> {
			return LazyIndexer.init((propertyName) -> {
				return LazyIndexer.init((propertyType) -> {
					var memberField = ListUtil.find(master.objectFields(), (field) -> {
						return field.propertyName().equalsIgnoreCase(propertyName);
					});

					if (memberField == null) {

						throw new IllegalStateException(
								Language.strings("codeart.ddd", "NotFoundTableField", master.name(), propertyName));
					}

					Class<?> objectType = TypeUtil.isCollection(propertyType) ? memberField.tip().monotype()
							: propertyType;

					return createChildTable(master, memberField, objectType);
				});
			});
		});
	}

	/**
	 * 
	 * 表示表是不是因为多条值数据（例如：List<int>）而创建的中间表
	 * 
	 * @return
	 */
	public boolean isPrimitiveValue() {

		return this.isMultiple() && this.slave() == null; // 作为多条值数据的表，没有slave子表

	}

	private DataTable createChildTable(DataTable master, IDataField memberField, Class<?> objectType) {
		DataTable root = null;
		if (this.root() == null || this.isAggregateRoot())
			root = this; // 就算有this.Root也要判断表是否为根对象的表，如果是为根对象的表，那么root就是自己
		else
			root = this.root();

		DataTable table = null;
		switch (memberField.fieldType()) {
		case DataFieldType.ValueObject: {
			table = DataTableLoader.createValueObject(root, master, memberField, objectType);
			break;
		}
		case DataFieldType.EntityObject: {
			table = DataTableLoader.createEntityObject(root, master, memberField, objectType);
			break;
		}
		case DataFieldType.EntityObjectList: {
			table = DataTableLoader.createEntityObjectList(root, master, memberField, objectType);
			break;
		}
		case DataFieldType.ValueObjectList: {
			table = DataTableLoader.createValueObjectList(root, master, memberField, objectType);
			break;
		}
		case DataFieldType.ValueList: {
			table = DataTableLoader.createValueList(root, master, memberField, objectType);
			break;
		}
		case DataFieldType.AggregateRoot: {
			table = DataTableLoader.createAggregateRoot(root, master, memberField, objectType);
			break;
		}
		case DataFieldType.AggregateRootList: {
			table = DataTableLoader.createAggregateRootList(root, master, memberField, objectType);
			break;
		}
		default:
			break;
		}

		if (table == null)
			throw new IllegalStateException(Language.strings("codeart.ddd", "createChildTable", master.name(),
					memberField.fieldType(), objectType.getName()));

		return table;
	}

//	private void InitDynamic()
//	{
//	    this.DynamicType = this.ObjectType as RuntimeObjectType;
//	    this.IsDynamic = this.DynamicType != null;
//	    if (this.IsDynamic)
//	    {
//	        AddTypTable(this.DynamicType.Define.TypeName, this);
//	    }
//	}

	private IDataMapper _mapper;

	public IDataMapper mapper() {
		return _mapper;
	}

	private DataTableQuery _query;

	private DataTableInsert _insert;

	DataTable(Class<?> objectType, DataTableType type, String name, Iterable<IDataField> objectFields,
			DataTable chainRoot, DataTable master, IDataField memberField) {

		_id = DataTableUtil.getId(memberField, chainRoot, name);
		_objectType = objectType;
		_type = type;
		_name = name;

		_fields = getFields(objectFields);
		_objectFields = objectFields;

		_chainRoot = chainRoot;
		_master = master;

		initMemberField(name, master, memberField);

		_root = findActualRoot(chainRoot);
		initObjectType(objectType, memberField == null ? null : memberField.tip());

		this._chain = this.memberField() == null ? ObjectChain.Empty : new ObjectChain(this.memberField());

		this._isMultiple = this.memberField() == null ? false : this.memberField().isMultiple();

		initTableIdName();
		_mapper = DataMapperFactory.create(this.objectType());

		_query = new DataTableQuery(this);
		_insert = new DataTableInsert(this);
	}

	void loadChilds() {
		initChilds();
	}

	/**
	 * 是否为同一个非运行时表，也就是说只用根据名称判断
	 * 
	 * @param target
	 * @return
	 */
	public boolean same(DataTable target) {
		return this.name().equals(target.name());
	}

	public boolean masterIsRoot() {
		return this.root().same(this.master());
	}

	void insert(DomainObject object) {
		_insert.insert(object);
	}

	void insertMiddle(IDomainObject root, IDomainObject master, Iterable<?> slaves) {
		_insert.insertMiddle(root, master, slaves);
	}

	void insertMember(DomainObject root, DomainObject parent, DomainObject obj) {
		_insert.insertMember(root, parent, obj);
	}

	Object querySingle(Object rootId, Object id) {
		return _query.querySingle(rootId, id);
	}

//	DataTableQuery query() {
//		return _query;
//	}

}
