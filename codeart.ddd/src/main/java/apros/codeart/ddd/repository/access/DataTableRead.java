package apros.codeart.ddd.repository.access;

import static apros.codeart.runtime.Util.propagate;

import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

import apros.codeart.ddd.*;
import apros.codeart.dto.DTObject;
import apros.codeart.runtime.EnumUtil;
import apros.codeart.util.TimeUtil;
import com.google.common.collect.Iterables;

import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.ConstructorParameterInfo;
import apros.codeart.ddd.repository.ConstructorRepositoryImpl;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

final class DataTableRead {
    private DataTable _self;

    public DataTableRead(DataTable self) {
        _self = self;
    }

    // <summary>
    /// 1对多的引用关系的读取
    /// </summary>
    /// <param name="rootId"></param>
    /// <param name="id"></param>
    /// <returns></returns>
    @SuppressWarnings("unchecked")
    Object readOneToMore(PropertyMeta tip, ConstructorParameterInfo prmTip, DomainObject parent, Object rootId,
                         Object masterId, QueryLevel level) {
        var datas = queryRootAndSlaveIds(rootId, masterId);

        Class<?> implementType = null;
        if (parent == null) {
            // 说明还在构造阶段,或者是内部调用
            if (prmTip != null && prmTip.implementType() != null)
                implementType = prmTip.implementType();
            else
                implementType = _self.middle().objectType(); // middle表对应的是属性的基类类型
        } else {
            implementType = _self.middle().objectType(); // middle表对应的是属性的基类类型
        }
        var list = createList(parent, implementType, tip);
        var elementType = _self.middle().elementType();

        if (_self.type() == DataTableType.AggregateRoot) {
            // 引用了多个外部内聚根
            var model = DataModelLoader.get((Class<? extends IAggregateRoot>) elementType);
            var root = model.root();
            var slaveIdName = GeneratedField.SlaveIdName;

            var queryLevel = getQueryAggregateRootLevel(level);
            for (var data : datas) {
                var slaveId = data.get(slaveIdName);
                var item = (IDomainObject) root.querySingle(slaveId, queryLevel);
                if (!item.isEmpty()) {
                    list.add(item);
                }
            }
        } else {
            var slaveIdName = GeneratedField.SlaveIdName;
            for (var data : datas) {
                var slaveId = data.get(slaveIdName);
                var item = (IDomainObject) _self.querySingle(rootId, slaveId);
                if (!item.isEmpty()) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    /**
     * 读取基础数据的集合值
     *
     * @param tip
     * @param prmTip
     * @param parent
     * @param rootId
     * @param masterId
     * @param level
     * @return
     */
    @SuppressWarnings("unchecked")
    Object readValues(PropertyMeta tip, ConstructorParameterInfo prmTip, DomainObject parent, Object rootId,
                      Object masterId, QueryLevel level) {
        var datas = queryPrimitiveValues(rootId, masterId);

//        Class<?> implementType = null;
//        if (parent == null) {
//            // 说明还在构造阶段,或者是内部调用
//            if (prmTip != null && prmTip.implementType() != null) {
//                implementType = prmTip.implementType();
//            } else
//                implementType = _self.objectType();
//        } else {
//            implementType = _self.objectType();
//        }
        var elementType = _self.elementType();
        var list = createList(parent, elementType, tip);

        var valueName = GeneratedField.PrimitiveValueName;

        if (elementType.isEnum()) {
            for (var data : datas) {
                var value = data.get(valueName);
                var e = EnumUtil.fromValue(elementType, value);
                list.add(e);
            }
        } else {
            for (var data : datas) {
                var value = data.get(valueName);
                list.add(value);
            }
        }

        return list;
    }

    /**
     * 集合在对象中的属性定义
     *
     * @param parent
     * @param tip
     * @return
     */
    @SuppressWarnings("rawtypes")
    private Collection createList(DomainObject parent, Class<?> elementType, PropertyMeta tip) {
//        var collection = new DomainCollection<>(elementType, DomainProperty.getProperty(tip)); // 不用该方法，目前DomainProperty.getProperty(tip)只会从子类获取领域属性，父类的领域属性并没有被加载，所以DomainProperty.getProperty(tip)可能为Null
        var collection = new DomainCollection<>(elementType, tip.name());
        collection.setParent(parent);
        return collection;
    }

//    private static Function<Class<?>, Boolean> _isDomainCollection = LazyIndexer.init((type) -> {
//        return DomainCollection.class.isAssignableFrom(type);
//    });

//    private static Function<Class<?>, Constructor<?>> _getDomainCollectionConstructor = LazyIndexer.init((elementType) -> {
//
//        try {
//            return DomainCollection.class.getConstructor(Class.class, DomainProperty.class);
//        } catch (Throwable ex) {
//            throw propagate(ex);
//        }
//    });

    /**
     * 创建对象
     *
     * @param objectType
     * @param data
     * @param level
     * @return
     */
    DomainObject createObject(Class<?> objectType, MapData data, QueryLevel level) {

        if (data.isEmpty())
            return (DomainObject) DomainObject.getEmpty(objectType);

        DomainObject obj = DataContext.using(() -> {
            return createObjectImpl(objectType, objectType, data, level);
        });
        return obj;
    }

    private DomainObject createObjectImpl(Class<?> defineType, Class<?> objectType, MapData data, QueryLevel level) {
        // 构造对象
        DomainObject obj = constructObject(objectType, data, level);

        // 设置代理对象
        setDataProxy(obj, data, level == QueryLevel.MIRRORING);

        // 为了避免死循环，我们先将对象加入到构造上下文中
        addToConstructContext(obj, data);

        // 加载属性
        loadProperties(defineType, data, obj, level);

        removeFromConstructContext(obj);

        // 补充信息
        supplement(obj, data, level);

        return obj;
    }

    private void addToConstructContext(DomainObject obj, MapData data) {
        Object id = data.get(EntityObject.IdPropertyName);
        if (_self.type() == DataTableType.AggregateRoot) {
            ConstructContext.add(id, obj);
        } else {
            Object rootId = data.get(GeneratedField.RootIdName);
            ConstructContext.add(rootId, id, obj);
        }
    }

    private void removeFromConstructContext(DomainObject obj) {
        ConstructContext.remove(obj);
    }

    private DomainObject constructObject(Class<?> objectType, MapData data, QueryLevel level) {

        try {
            var constructorTip = ConstructorRepositoryImpl.getTip(objectType, true);
            var constructor = constructorTip.constructor();
            constructor.setAccessible(true);
            var args = createArguments(constructorTip, data, level);
            return (DomainObject) constructor.newInstance(args);
        } catch (Throwable ex) {
            throw propagate(ex);
        }
    }

    /**
     * 加载属性
     *
     * @param objectType
     * @param data
     * @param obj
     * @param level
     */
    private void loadProperties(Class<?> objectType, MapData data, DomainObject obj, QueryLevel level) {
        var propertyTips = PropertyMeta.getProperties(objectType); // 此处不必考虑是否为派生类，直接赋值所有属性
        for (var propertyTip : propertyTips) {
            // 只有是可以公开设置的属性和不是延迟加载的属性我们才会主动赋值
            // 有些属性是私有设置的，这种属性有可能是仅获取外部的数据而不需要赋值的
            // 如果做了inner处理，那么立即加载
            if ((propertyTip.isPublicSet() && !propertyTip.lazy()) || containsObjectData(propertyTip, data)) {
                var value = readPropertyValue(obj, propertyTip, null, data, level); // 已不是构造，所以不需要prmTip参数
                if (value == null) {
                    throw new IllegalArgumentException(Language.strings("apros.codeart.ddd", "LoadPropertyError",
                            String.format("%s.%s", propertyTip.declaringType().getName(), propertyTip.name())));
                }

                obj.loadValue(propertyTip.name(), value, false);
            }
        }
    }

    private void setDataProxy(DomainObject obj, MapData data, boolean isMirror) {
        // 设置代理对象
        obj.dataProxy(new DataProxyImpl(data, _self, isMirror));
    }

    private void supplement(DomainObject obj, MapData data, QueryLevel level) {
        var valueObject = TypeUtil.as(obj, IValueObject.class);
        if (valueObject != null) {
            Object id = data.get(EntityObject.IdPropertyName);
            if (id != null) {
                id = convertData(id, UUID.class);
                valueObject.setPersistentIdentity((UUID) id);
            }
        }

        obj.markClean(); // 对象从数据库中读取，是干净的
    }

    private Object[] createArguments(ConstructorRepositoryImpl tip, MapData data, QueryLevel level) {
        var length = Iterables.size(tip.parameters());

        if (length == 0)
            return ListUtil.emptyObjects();
        Object[] args = new Object[length];
        var prms = tip.parameters();
        var prmsLength = prms.size();
        for (var i = 0; i < prmsLength; i++) {
            var prm = prms.get(i);
            var arg = createArgument(prm, data, level);
            args[i] = arg;
        }
        return args;
    }

    private Object createArgument(ConstructorParameterInfo prm, MapData data, QueryLevel level) {
        // 看构造特性中是否定义了加载方法
        var value = prm.loadData(_self.objectType(), data, level);
        if (value != null) {
            return value;
        }
        var tip = prm.propertyTip();
        if (tip == null)
            throw new IllegalStateException(Language.stringsMessageFormat("apros.codeart.ddd",
                    "ConstructionParameterNoProperty", _self.objectType().getName(), prm.name()));

        // 从属性定义中加载
        value = readPropertyValue(null, tip, prm, data, level); // 在构造时，还没有产生对象，所以parent为 null
        if (value == null)
            throw new IllegalStateException(Language.stringsMessageFormat("apros.codeart.ddd", "ConstructionParameterError",
                    prm.declaringType().getName(), prm.name()));
        return value;
    }

    /// <summary>
    ///
    /// </summary>
    /// <param name="parent"></param>
    /// <param name="tip"></param>
    /// <param name="prmTip"></param>
    /// <param name="data"></param>
    /// <param name="level">对象在被加载时用到的查询级别</param>
    /// <returns></returns>
    public Object readPropertyValue(DomainObject parent, PropertyMeta tip, ConstructorParameterInfo prm, MapData data,
                                    QueryLevel level) {
        // 看对应的属性特性中是否定义了加载方法，优先执行自定义方法
        var loader = tip.dataLoader();
        if (loader != null) {
            return loader.load(data, level);
        }

        // 自动加载
        switch (tip.category()) {
            case DomainPropertyCategory.Primitive: {
                return readPrimitive(tip, data);
            }
            case DomainPropertyCategory.PrimitiveList: {
                return readPrimitiveList(parent, tip, prm, data, level);
            }
            case DomainPropertyCategory.AggregateRoot: {
                return readAggregateRoot(tip, data, level);
            }
            case DomainPropertyCategory.ValueObject:
            case DomainPropertyCategory.EntityObject: {
                return readMember(tip, data);
            }
            case DomainPropertyCategory.EntityObjectList:
            case DomainPropertyCategory.ValueObjectList:
            case DomainPropertyCategory.AggregateRootList: {
                return readMembers(parent, tip, prm, data, level);
            }
        }
        return null;
    }

    //region 读取基础的值数据

    private Object readPrimitive(PropertyMeta tip, MapData data) {
        var value = tip.lazy() ? readValueByLazy(tip, data) : readValueFromData(tip, data);
        if (value == null) {
            // Emptyable类型的数据有可能存的是null值
            if (tip.isEmptyable())
                return Emptyable.createEmpty(tip.monotype());
            throw new IllegalStateException("Missing value for property " + tip.name());
        }
        return value;
    }

    private Object readValueFromData(PropertyMeta tip, MapData data) {
        var value = data.get(tip.name());
        return convertData(value, tip.monotype());
    }

    private static Object convertData(Object value, Class<?> exceptType) {

        if (value == null) return null;

        if (exceptType == UUID.class && value.getClass() == String.class) {
            return UUID.fromString(value.toString());
        }

        if (exceptType.isEnum()) {
            return EnumUtil.fromValue(exceptType, value);
        }

        if (exceptType == EmptyableInt.class) {
            return new EmptyableInt((Integer) value);
        }

        if (exceptType == EmptyableLong.class) {
            return new EmptyableLong((Long) value);
        }

        if (exceptType == OffsetDateTime.class) {
            var time = (Timestamp) value;
            return time.toInstant().atOffset(ZoneOffset.UTC);
        }

        if (exceptType == EmptyableOffsetDateTime.class) {
            var time = (Timestamp) value;
            return new EmptyableOffsetDateTime(time.toInstant().atOffset(ZoneOffset.UTC));
        }

        if (exceptType == LocalDateTime.class) {
            var time = (Timestamp) value;
            return time.toLocalDateTime();
        }

        if (exceptType == EmptyableDateTime.class) {
            var time = (Timestamp) value;
            return new EmptyableDateTime(time.toLocalDateTime());
        }

        if (exceptType == ZonedDateTime.class) {
            var time = (Timestamp) value;
            return TimeUtil.toUTC(time.toInstant());
        }

        if (exceptType == EmptyableZonedDateTime.class) {
            var time = (Timestamp) value;
            return new EmptyableZonedDateTime(TimeUtil.toUTC(time.toInstant()));
        }

        if (exceptType == DTObject.class) {
            var code = value.toString();
            // 由于DTObject类型的属性，是用来对值类型对象的补充（快速建立动态值对象）
            // 所以DTObject类型的属性也要遵守值类型的约定：不能更改值类型对象的属性，只能完全替换。
            return DTObject.readonly(code);
        }

        return value;
    }

    private Object readValueByLazy(PropertyMeta tip, MapData data) {
        Object id = data.get(EntityObject.IdPropertyName);
        if (id != null) {

            var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
                    : GeneratedField.RootIdName;
            Object rootId = data.get(rootIdName);
            if (rootId != null) {
                return queryDataScalar(rootId, id, tip.name());
            }
        }
        return null;
    }

//	region 读取基础值的集合数据

    private Object readPrimitiveList(DomainObject parent, PropertyMeta tip, ConstructorParameterInfo prmTip,
                                     MapData data, QueryLevel level) {
        var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
                : GeneratedField.RootIdName;

        Object rootId = data.get(rootIdName);

        if (rootId != null) {
            // 当前对象的编号，就是子对象的masterId
            Object masterId = data.get(EntityObject.IdPropertyName);

            var child = _self.findChild(_self, tip);
            return child.readValues(tip, prmTip, parent, rootId, masterId, level);
        }
        return null;
    }

//	#endregion

    private static QueryLevel getQueryAggregateRootLevel(QueryLevel masterLevel) {
        // 除了镜像外，通过属性读取外部根，我们都是无锁的查询方式
        return masterLevel == QueryLevel.MIRRORING ? QueryLevel.MIRRORING : QueryLevel.NONE;
    }


    /**
     * 获得子对象的数据
     *
     * @param data
     * @param tip
     * @return
     */
    private MapData getObjectData(MapData data, PropertyMeta tip) {
        // 以前缀来最大化收集，因为会出现类似Good_Unit_Name 这种字段，不是默认字段，但是也要收集，是通过inner good.unit的语法来的
        var prefix = String.format("%s_", tip.name());

        MapData value = null;
        for (var p : data) {
            if (p.getKey().startsWith(prefix)) {
                if (value == null)
                    value = new MapData();

                var name = p.getKey().substring(prefix.length());

                value.put(name, p.getValue());
            }
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    private boolean containsObjectData(PropertyMeta tip, MapData data) {
        DataTable table = null;

        switch (tip.category()) {
            case DomainPropertyCategory.AggregateRoot: {
                var model = DataModelLoader.get((Class<? extends IAggregateRoot>) tip.monotype());
                table = model.root();
                break;
            }
            case DomainPropertyCategory.EntityObject:
            case DomainPropertyCategory.ValueObject: {
                table = _self.findChild(_self, tip);
                break;
            }
            default:
                return false;
        }

        // 以默认字段来验证
        var fields = table.defaultQueryFields();

        for (var field : fields) {
            var name = String.format("%s_%s", tip.name(), field.name());
            if (!data.containsKey(name))
                return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Object readAggregateRoot(PropertyMeta tip, MapData data, QueryLevel level) {
        var model = DataModelLoader.get((Class<? extends IAggregateRoot>) tip.monotype());
        var item = getObjectData(data, tip);
        if (item != null &&
                item.containsKey(EntityObject.IdPropertyName)) {  // 追加的item.containsKey(EntityObject.IdPropertyName)判断很重要，有可能因为查询条件导致多出的数据，不能作为完整对象输出
            MapData entry = (MapData) item;
            return model.root().createObject(tip.monotype(), entry, QueryLevel.NONE); // 从数据中直接加载的根对象信息，一定是不带锁的
        }

        var dataKey = DataTableUtil.getIdName(tip.name());

        Object id = data.get(dataKey);
        if (id != null) {
            var queryLevel = getQueryAggregateRootLevel(level);
            return model.root().querySingle(id, queryLevel);
        }
        return null;
    }

    private Object readMember(PropertyMeta tip, MapData data) {
        return tip.lazy() ? readMemberByLazy(tip, data) : readMemberFromData(tip, data);
    }

    private Object readMemberFromData(PropertyMeta tip, MapData data) {
        var name = DataTableUtil.getNameWithSeparated(tip.name());
        var subData = new MapData();
        for (var p : data) {
            var dataName = p.getKey();
            if (dataName.startsWith(name)) {
                var subName = DataTableUtil.getNextName(dataName);
                subData.put(subName, p.getValue());
            }
        }

        if (subData.isEmpty())
            return DomainObject.getEmpty(tip.monotype());

        var typeKey = (String) subData.get(GeneratedField.TypeKeyName);
        Class<?> objectType = StringUtil.isNullOrEmpty(typeKey) ? tip.monotype()
                : DerivedClassImpl.getDerivedType(typeKey);

        var child = _self.findChild(_self, tip.name());
        // 先尝试中构造上下文中得到
        var obj = child.getObjectFromConstruct(subData);
        if (obj == null)
            obj = child.createObject(objectType, subData, QueryLevel.NONE); // 成员始终是QueryLevel.None的方式加载
        return obj;
    }

    private Object readMemberByLazy(PropertyMeta tip, MapData data) {
        var child = _self.findChild(_self, tip);
        var dataKey = DataTableUtil.getIdName(tip.name());

        Object id = data.get(dataKey);
        if (id != null) {

            var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
                    : GeneratedField.RootIdName;

            Object rootId = data.get(rootIdName);
            if (rootId != null) {
                return child.querySingle(rootId, id);
            }
        }
        return null;
    }

    Object readMembers(DomainObject parent, PropertyMeta tip, ConstructorParameterInfo prmTip, MapData data,
                       QueryLevel level) {

        var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
                : GeneratedField.RootIdName;

        var rootId = data.get(rootIdName);

        if (rootId != null) {
            // 当前对象的编号，就是子对象的masterId
            Object masterId = data.get(EntityObject.IdPropertyName);

            var child = _self.findChild(_self, tip);
            return child.readOneToMore(tip, prmTip, parent, rootId, masterId, level);
        }
        return null;
    }

    /**
     * 读取对象集合的内部调用版本，此方法不是用于构造对象，而是为了查询用
     *
     * @param tip
     * @param data
     * @param objs
     */
    public void queryMembers(PropertyMeta tip, MapData data, ArrayList<Object> objs) {

        var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
                : GeneratedField.RootIdName;

        Object rootId = data.get(rootIdName);

        // 当前对象的编号，就是子对象的masterId
        Object masterId = data.get(EntityObject.IdPropertyName);

        var child = _self.findChild(_self, tip);
        child.queryOneToMore(rootId, masterId, objs);
    }

    public void queryOneToMore(Object rootId, Object masterId, ArrayList<Object> objs) {
        var datas = queryRootAndSlaveIds(rootId, masterId);

        var slaveIdName = GeneratedField.SlaveIdName;
        for (var data : datas) {
            var slaveId = data.get(slaveIdName);
            var item = _self.querySingle(rootId, slaveId);
            objs.add(item);
        }
    }

    public Object queryMember(PropertyMeta tip, MapData data) {
        var child = _self.findChild(_self, tip); // 通过基本表就可以实际查出数据，查数据会自动识别数据的真实类型的

        var rootIdName = _self.type() == DataTableType.AggregateRoot ? EntityObject.IdPropertyName
                : GeneratedField.RootIdName;

        var rootId = data.get(rootIdName);

        if (rootId != null) {

            var field = DataTableUtil.getQuoteField(_self, tip.name());

            Object id = data.get(field.name());

            if (id != null) {
                return child.querySingle(rootId, id);
            }
        }

        return null;
    }

//	#region 查询数据

    /// <summary>
    /// 查询单值数据，不必缓存，因为延迟加载后就被加载到内存中已被缓存的对象了
    /// </summary>
    /// <param name="rootId"></param>
    /// <param name="id"></param>
    /// <param name="propertyName"></param>
    /// <returns></returns>
    private Object queryDataScalar(Object rootId, Object id, String propertyName) {

        var param = new MapData();
        if (_self.type() != DataTableType.AggregateRoot) {
            param.put(GeneratedField.RootIdName, rootId);
        }
        param.put(EntityObject.IdPropertyName, id);

        var expression = getScalarByIdExpression(_self, propertyName);

        var qb = DataSource.getQueryBuilder(QueryObjectQB.class);
        var description = QueryDescription.createBy(param, expression, QueryLevel.NONE, _self);
        var sql = qb.build(description);

        return DataAccess.using((access) -> {
            return access.nativeQueryScalar(sql, param);
        });
    }

    /**
     * 查询1对多引用的成员数据
     *
     * @param rootId
     * @param masterId
     * @param datas
     */
    private Iterable<MapData> queryRootAndSlaveIds(Object rootId, Object masterId) {
        // 查询涉及到中间表,对对象本身没有任何条件可言
        var qb = DataSource.getQueryBuilder(GetSlaveIdsQB.class);
        var sql = qb.build(new QueryDescription(_self));

        var param = new MapData();
        param.put(GeneratedField.RootIdName, rootId);
        // if (!this.Root.IsEqualsOrDerivedOrInherited(this.Master))
        if (!_self.root().same(_self.master())) {
            param.put(GeneratedField.MasterIdName, masterId);
        }

        return DataAccess.using((access) -> {
            return access.nativeQueryRows(sql, param);
        });
    }

    /**
     * 查询基础值的集合的数据
     *
     * @param rootId
     * @param masterId
     * @return
     */
    private Iterable<MapData> queryPrimitiveValues(Object rootId, Object masterId) {
        // 查询涉及到中间表,对对象本身没有任何条件可言
        var qb = DataSource.getQueryBuilder(GetPrimitiveValuesQB.class);
        var sql = qb.build(new QueryDescription(_self));

        var param = new MapData();
        param.put(GeneratedField.RootIdName, rootId);
        if (!_self.root().same(_self.master())) {
            param.put(GeneratedField.MasterIdName, masterId);
        }

        return DataAccess.using((access) -> {
            return access.nativeQueryRows(sql, param);
        });
    }

//	#endregion

    private static String getScalarByIdExpression(DataTable table, String propertyName) {
        return _getScalarById.apply(table).apply(propertyName);
    }

    private static final Function<DataTable, Function<String, String>> _getScalarById = LazyIndexer.init((table) -> {
        return LazyIndexer.init((propertyName) -> {
            String expression = null;

            if (table.type() == DataTableType.AggregateRoot) {
                expression = StringUtil.format("[{0}=@{0}][select {1}]", EntityObject.IdPropertyName, propertyName);
            } else {
                expression = StringUtil.format("[{0}=@{0} and {1}=@{1}][select {2}]", GeneratedField.RootIdName,
                        EntityObject.IdPropertyName, propertyName);
            }

            return expression;
        });
    });

}
