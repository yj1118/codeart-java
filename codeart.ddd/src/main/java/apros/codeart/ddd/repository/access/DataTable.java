package apros.codeart.ddd.repository.access;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.metadata.ObjectRepositoryTip;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.ConstructorParameterInfo;
import apros.codeart.ddd.repository.Page;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.TypeMismatchException;

public class DataTable {

    private String _id;

    /**
     * 表的唯一表号，由引用路径生成
     *
     * @return
     */
    public String id() {
        return _id;
    }

    private Class<?> _objectType;

    /**
     * 表对应对实体类型，注意中间表对应的是集合类型
     *
     * @return
     */
    public Class<?> objectType() {
        return _objectType;
    }

    public Class<? extends IAggregateRoot> rootType() {
        if (IAggregateRoot.class.isAssignableFrom(_objectType)) {
            return (Class<? extends IAggregateRoot>) _objectType;
        }
        throw new TypeMismatchException(IAggregateRoot.class, _objectType);
    }

    private ObjectRepositoryTip _repositoryTip;

    public ObjectRepositoryTip repositoryTip() {
        return _repositoryTip;
    }

    private DataTableType _type;

    /**
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
     * 在引用链中的根表，这个属性大多数情况等属性Root，但是在跨根引用中会有所不同
     * <p>
     * 例如：根 role 引用了根 organization,而根 organization 又有属性 permissions 引用了多个根permission
     * <p>
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
     * 所属主表
     *
     * @return
     */
    public DataTable master() {
        return _master;
    }

    private DataTable _middle;

    /**
     * 与该表相关的中间表
     *
     * @return
     */
    public DataTable middle() {
        return _middle;
    }

    void setMiddle(DataTable value) {
        _middle = value;
    }

    private DataTable _slave;

    /**
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
     * 该值仅在中间表时才有用
     *
     * @return
     */
    public Class<?> elementType() {
        return _elementType;
    }

    private ObjectChain _chain;

    /**
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

    private final Iterable<IDataField> _fields;

    public Iterable<IDataField> fields() {
        return _fields;
    }

    public IDataField getField(String fieldName,boolean ignoreCase) {
        if(ignoreCase)
            return ListUtil.find(this.fields(),(f)-> f.name().equalsIgnoreCase(fieldName));
        else
            return ListUtil.find(this.fields(),(f)-> f.name().equals(fieldName));
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

            temp.trimToSize();
            return temp;
        }

        return fields;
    }

    private Iterable<IDataField> _defaultQueryFields;

    /// <summary>
    /// 默认查询的字段
    /// </summary>
    public Iterable<IDataField> defaultQueryFields() {
        if (_defaultQueryFields == null) {
            _defaultQueryFields = ListUtil.filter(this.fields(), (t) -> !t.isAdditional() && !t.tip().lazy());
        }
        return _defaultQueryFields;
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
     * <p>
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
     * 在运行时查找子表信息
     *
     * @param master
     * @param tip
     * @return
     */
    public DataTable findChild(DataTable master, PropertyMeta tip) {
        return _getRuntimeTable.apply(master).apply(tip.name());
    }


    public DataTable findChild(String propertyName) {
        return findChild(this, propertyName);
    }

    /**
     * 根据属性名称，实际类型，获取对应的表信息
     * <p>
     * runtime表包括预定义的类型和运行时加载的类型对应的表
     * <p>
     * 本来通过master和master下的属性名称PropertyName，我们就可以找到子表的定义
     * <p>
     * 但是由于属性的值有可能是不同的类型（因为继承关系），所以我们还需要指定属性的类型objectType
     * <p>
     * 才能获得当前属性对应的表信息
     *
     * @param master
     * @param propertyName
     * @return
     */
    public DataTable findChild(DataTable master, String propertyName) {
        return _getRuntimeTable.apply(master).apply(propertyName);
    }

    private Function<DataTable, Function<String, DataTable>> _getRuntimeTable;

    private void initGetRuntimeTable() {
        _getRuntimeTable = LazyIndexer.init((master) -> {
            return LazyIndexer.init((propertyName) -> {
                var memberField = ListUtil.find(master.objectFields(), (field) -> {
                    return field.propertyName().equalsIgnoreCase(propertyName);
                });

                if (memberField == null) {

                    throw new IllegalStateException(Language.strings("apros.codeart.ddd", "NotFoundTableField",
                            master.name(), propertyName));
                }

                Class<?> objectType = memberField.tip().monotype();

                return createChildTable(master, memberField, objectType);
            });
        });
    }

    /**
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
            throw new IllegalStateException(Language.strings("apros.codeart.ddd", "createChildTable", master.name(),
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

    private DataTableUpdate _update;

    private DataTableRead _read;

    private DataTableDelete _delete;

    private DataTableCommon _common;

    public DataTable(Class<?> objectType, DataTableType type, String name, Iterable<IDataField> objectFields,
                     DataTable chainRoot, DataTable master, IDataField memberField) {

        _id = DataTableUtil.getId(memberField, chainRoot, name);
        _objectType = objectType;
        _type = type;
        _name = name;

        _chainRoot = chainRoot;
        _master = master;

        _root = findActualRoot(chainRoot);

        _fields = getFields(objectFields);
        _objectFields = objectFields;


        initMemberField(name, master, memberField);

        initObjectType(objectType, memberField == null ? null : memberField.tip());

        this._chain = this.memberField() == null ? ObjectChain.Empty : new ObjectChain(this.memberField());

        this._isMultiple = this.memberField() == null ? false : this.memberField().isMultiple();

        initTableIdName();

        if (this.isAggregateRoot()) {
            _mapper = DataMapperFactory.create(this.rootType());
        } else {
            _mapper = DataMapperFactory.create(_root.rootType());
        }

        _query = new DataTableQuery(this);
        _insert = new DataTableInsert(this);
        _update = new DataTableUpdate(this);
        _delete = new DataTableDelete(this);
        _read = new DataTableRead(this);
        _common = new DataTableCommon(this);
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

    void insertMembers(DomainObject root, DomainObject parent, DomainObject current, PropertyMeta tip) {
        _insert.insertMembers(root, parent, current, tip);
    }

    void insertMembers(DomainObject root, DomainObject parent, DomainObject current, Iterable<?> members,
                       PropertyMeta tip) {
        _insert.insertMembers(root, parent, current, members, tip);
    }

    <T> T querySingle(Object id, QueryLevel level)
    {
        return _query.querySingle(id, level);
    }

    <T> T querySingle(Object rootId, Object id) {
        return _query.querySingle(rootId, id);
    }

    <T> T querySingle(String expression, Consumer<MapData> fillArg, QueryLevel level) {
        return _query.querySingle(expression, fillArg, level);
    }

    <T> Iterable<T> query(String expression, Consumer<MapData> fillArg, QueryLevel level) {
        return _query.query(expression, fillArg, level);
    }

    <T> Page<T> query(String expression, int pageIndex, int pageSize, Consumer<MapData> fillArg) {
        return _query.query(expression, pageIndex, pageSize, fillArg);
    }

    int getCount(String expression, Consumer<MapData> fillArg, QueryLevel level) {
        return _query.getCount(expression, fillArg, level);
    }

//	void execute(String expression, Consumer<MapData> fillArg, QueryLevel level,
//			Consumer<Map<String, Object>> fillItems) {
//		_query.execute(expression, fillArg, level, fillItems);
//	}

    void queryOneToMore(Object rootId, Object masterId, ArrayList<Object> objs) {
        _read.queryOneToMore(rootId, masterId, objs);
    }

    void queryMembers(PropertyMeta tip, MapData data, ArrayList<Object> objs) {
        _read.queryMembers(tip, data, objs);
    }

    Object queryMember(PropertyMeta tip, MapData data) {
        return _read.queryMember(tip, data);
    }

    int getDataVersion(MapData originalData) {
        return _query.getDataVersion(originalData);
    }

    int getDataVersion(Object id) {
        return _query.getDataVersion(id);
    }

    int getDataVersion(Object rootId, Object id) {
        return _query.getDataVersion(rootId, id);
    }

    public void checkDataVersion(DomainObject root) {
        _query.checkDataVersion(root);
    }

    public int getAssociated(Object rootId, Object id) {
        return _query.getAssociated(rootId, id);
    }

    public void decrementAssociated(Object rootId, Object id) {
        _update.decrementAssociated(rootId, id);
    }

    void delete(DomainObject root) {
        _delete.delete(root);
    }

    void deleteMiddle(DomainObject root, DomainObject master, DomainObject slave) {
        _delete.deleteMiddle(root, master, slave);
    }

    void deleteMiddleByRootSlave(DomainObject obj) {
        _delete.deleteMiddleByRootSlave(obj);
    }

    void deleteMiddleByMaster(DomainObject root, DomainObject master) {
        _delete.deleteMiddleByMaster(root, master);
    }

    void deleteMember(DomainObject root, DomainObject parent, DomainObject obj) {
        _delete.deleteMember(root, parent, obj);
    }

    void deleteMemberByOriginalData(DomainObject root, DomainObject parent, DomainObject current, PropertyMeta tip) {
        _delete.deleteMemberByOriginalData(root, parent, current, tip);
    }

    void deleteMembersByOriginalData(DomainObject root, DomainObject parent, DomainObject current, PropertyMeta tip) {
        _delete.deleteMembersByOriginalData(root, parent, current, tip);
    }

    void deleteMembers(DomainObject root, DomainObject parent, DomainObject current, Iterable<?> members,
                       PropertyMeta tip) {
        _delete.deleteMembers(root, parent, current, members, tip);
    }

    void update(DomainObject obj) {
        _update.update(obj);
    }

    void updateMember(DomainObject root, DomainObject parent, DomainObject obj) {
        _update.updateMember(root, parent, obj);
    }

    void updateMembers(DomainObject root, DomainObject parent, DomainObject current, PropertyMeta tip) {
        _update.updateMembers(root, parent, current, tip);
    }

    void updateMembers(DomainObject root, DomainObject parent, DomainObject current, Iterable<?> members,
                       PropertyMeta tip) {
        _update.updateMembers(root, parent, current, members, tip);
    }

    Object readMembers(DomainObject parent, PropertyMeta tip, ConstructorParameterInfo prmTip, MapData data,
                       QueryLevel level) {
        return _read.readMembers(parent, tip, prmTip, data, level);
    }

    Object readValues(PropertyMeta tip, ConstructorParameterInfo prmTip, DomainObject parent, Object rootId,
                      Object masterId, QueryLevel level) {
        return _read.readValues(tip, prmTip, parent, rootId, masterId, level);
    }

    Iterable<DataTable> getQuoteMiddlesByMaster() {
        return _common.getQuoteMiddlesByMaster();
    }

    DomainObject createObject(Class<?> objectType, MapData data, QueryLevel level) {
        return _read.createObject(_objectType, data, level);
    }

    Object readPropertyValue(DomainObject parent, PropertyMeta tip, ConstructorParameterInfo prmTip, MapData data,
                             QueryLevel level) {
        return _read.readPropertyValue(parent, tip, prmTip, data, level);
    }

    Object getObjectFromConstruct(Object id) {
        return _query.getObjectFromConstruct(id);
    }

    Object readOneToMore(PropertyMeta tip, ConstructorParameterInfo prmTip, DomainObject parent, Object rootId,
                         Object masterId, QueryLevel level) {
        return _read.readOneToMore(tip, prmTip, parent, rootId, masterId, level);
    }

    /// <summary>
    /// 从构造上下文中获取对象
    /// </summary>
    /// <param name="id"></param>
    /// <returns></returns>
    Object getObjectFromConstruct(Object rootId, Object id) {
        return _query.getObjectFromConstruct(rootId, id);
    }

    Object getObjectFromConstruct(MapData data) {
        return _query.getObjectFromConstruct(data);
    }

    DomainObject loadObject(Object id, QueryLevel level) {
        return _query.loadObject(id, level);
    }

    DomainObject loadObject(Object rootId, Object id) {
        return _query.loadObject(rootId, id);
    }

    /**
     * 获得不带任何引用链的、独立、干净的表信息
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public DataTable getAbsolute() {
        switch (this.type()) {
            case DataTableType.AggregateRoot: {
                return DataModelLoader.get((Class<? extends IAggregateRoot>) this.objectType()).root();
            }
            case DataTableType.EntityObject:
            case DataTableType.ValueObject: {
                return this; // 对于成员类型，直接返回，因为没有干净的
            }
            default:
                break;
        }
        throw new IllegalStateException(Language.strings("apros.codeart.ddd", "UnknownException"));
    }

}
