package com.apros.codeart.ddd.repository.access;

import java.util.ArrayList;

import com.apros.codeart.ddd.metadata.ObjectMeta;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.ddd.metadata.ObjectRepositoryTip;
import com.apros.codeart.ddd.metadata.PropertyMeta;
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

	DataTable(Class<?> objectType, DataTableType type, String name, Iterable<IDataField> objectFields, DataTable chainRoot, DataTable master,
			IDataField memberField) {
		
		_id = DataTableUtil.getId(memberField, chainRoot, name);
		_objectType = objectType;
		_type = type;
		_name = name;
		
		_fields = getFields(objectFields);

		 
		 _chainRoot = chainRoot;
		 _master = master;
		 
		 if(memberField != null) memberField.table(this);
		 _memberField = memberField;

		 _root = findActualRoot(chainRoot);
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
