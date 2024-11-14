package apros.codeart.ddd.repository.access;

import static apros.codeart.i18n.Language.strings;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import apros.codeart.ddd.*;
import apros.codeart.ddd.virtual.VirtualRoot;
import com.google.common.collect.Iterables;

import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.EnumUtil;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;

final class DataTableUtil {
    private DataTableUtil() {
    }

    public static String getId(IDataField memberField, DataTable chainRoot, String tableName) {
        return getId(memberField, (chainRoot == null ? null : chainRoot.name()), tableName);
    }

    public static String getId(IDataField memberField, String rootTableName, String tableName) {
        if (memberField == null)
            return tableName; // 这肯定是根
        var chain = new ObjectChain(memberField);
        // 由于不同类型的表名不同，所以没有加入对象名称作为计算结果
        // 由于isSnapshot的值不同表名也不同，所以没有加入对象名称作为计算结果
        return String.format("%s_%s+%s", rootTableName, chain.path(), tableName);
    }

    /**
     * 将从对象里提取的字段转换为数据库表的字段
     *
     * @param objectFields
     * @return
     */
    public static Iterable<IDataField> getTableFields(Iterable<IDataField> objectFields) {
        ArrayList<IDataField> fields = new ArrayList<IDataField>();
        for (var objectField : objectFields)
            fillFields(fields, objectField);

        fields.trimToSize();
        return fields;
    }

    private static void fillFields(ArrayList<IDataField> fields, IDataField current) {
        String name = current.name();
        switch (current.fieldType()) {
            case DataFieldType.GeneratedField: {
                fields.add(current); // 对于生成的键，直接追加
                break;
            }
            case DataFieldType.Value: {
                var valueField = TypeUtil.as(current, ValueField.class);
                // 存值
                var field = new ValueField(current.tip(), Iterables.toArray(valueField.dbFieldTypes(), DbFieldType.class));

                field.name(name);
                field.parentMemberField(current.parentMemberField());

                fields.add(field);

                break;
            }
            case DataFieldType.EntityObject:
            case DataFieldType.AggregateRoot: {
                // 存外键即可
                var idTip = PropertyMeta.getProperty(current.tip().monotype(), EntityObject.IdPropertyName);

                var field = new ValueField(idTip);
                field.name(getIdName(name));
                field.parentMemberField(current);

                fields.add(field);

                break;
            }
            case DataFieldType.ValueObject: {
                var primaryKey = GeneratedField.createValueObjectPrimaryKey(current.tip().monotype());
                var field = new ValueField(primaryKey.tip());
                field.name(getIdName(name));
                field.parentMemberField(current);

                fields.add(field);

                break;
            }
            default: {
                break;
            }
        }
    }

    public static DbType getDbType(PropertyMeta meta) {
        Class<?> dataType = meta.monotype();

        if (meta.isEmptyable()) {
            dataType = Emptyable.getValueType(dataType);
        }

        if (dataType.isEnum()) {
            dataType = EnumUtil.getUnderlyingType();
        }

        DbType dbType = _typeMap.get(dataType);
        if (dbType != null)
            return dbType;

        throw new IllegalStateException(strings("apros.codeart.ddd", "DataTypeNotSupported", dataType.getName()));
    }

    public static Object getObjectId(Object obj) {
        var eo = TypeUtil.as(obj, IEntityObject.class);
        if (eo != null)
            return eo.getIdentity();

        var vo = TypeUtil.as(obj, IValueObject.class);
        if (vo != null)
            return vo.getPersistentIdentity(); // 生成的编号

        var dto = TypeUtil.as(obj, DTObject.class); // 测试时经常会用dto模拟对象
        if (dto != null)
            return dto.getValue(EntityObject.IdPropertyName);

        throw new IllegalStateException(strings("apros.codeart.ddd", "UnableGetId", obj.getClass().getSimpleName()));
    }

    public static ValueField getForeignKey(DataTable table, GeneratedFieldType keyType, DbFieldType... dbFieldTypes) {
        if (table.idField() == null)
            throw new IllegalStateException(Language.strings("apros.codeart.ddd", "TableNotId", table.name()));
        String name = table.tableIdName();
        switch (keyType) {
            case GeneratedFieldType.RootKey: {
                name = GeneratedField.RootIdName;
            }
            break;
            case GeneratedFieldType.MasterKey: {
                name = GeneratedField.MasterIdName;
            }
            break;
            case GeneratedFieldType.SlaveKey: {
                name = GeneratedField.SlaveIdName;
            }
            break;
            default:
                break;
        }
        return new GeneratedField(table.idField().tip(), name, keyType, dbFieldTypes);
    }

    @SuppressWarnings("unchecked")
    public static Iterable<Object> getValueListData(Object value, Class<?> elementType) {
        if (value == null)
            new ArrayList<String>(0);

        var list = TypeUtil.as(value, Iterable.class);
        if (elementType.isEnum()) {
            return ListUtil.map(list, (item) -> {
                return EnumUtil.getValue(item);
            });
        } else {
            return ListUtil.map(list, (item) -> {
                return (Object) item;
            });
        }
    }

    /// <summary>
    /// 获取基元类型的属性值
    /// </summary>
    /// <returns></returns>
    public static Object getPrimitivePropertyValue(DomainObject obj, PropertyMeta tip) {
        var value = obj.getValue(tip.name());
        if (!tip.isEmptyable())
            return value;
        var e = (IEmptyable) value;
        return e.isEmpty() ? null : e.getValue(); // 可以存null值在数据库
    }

    /**
     * 根据属性名称，获取对应的对象引用的字段，也就是类型 属性名Id的形式的字段
     *
     * @param table
     * @param propertyName
     * @return
     */
    public static IDataField getQuoteField(DataTable table, String propertyName) {
        return _getQuoteField.apply(table).apply(propertyName);
    }

    private static final Function<DataTable, Function<String, IDataField>> _getQuoteField = LazyIndexer.init((table) -> {
        return LazyIndexer.init((propertyName) -> {
            for (var field : table.fields()) {
                if (field.parentMemberField() == null)
                    continue;
                var current = field.parentMemberField();
                if (current.tip().name().equalsIgnoreCase(propertyName)) {
                    return field;
                }
            }
            return null;
        });
    });

//	#region 根据typeKey找表

    private static final HashMap<String, DataTable> _typeTables = new HashMap<String, DataTable>();

    static void addTypTable(String typeKey, DataTable table) {
        if (!_typeTables.containsKey(typeKey)) {
            // 防止 table.GetAbsolute方法操作了_typeTables，这里再次判断下重复
            var absoluteTable = table.getAbsolute();
            _typeTables.put(typeKey, absoluteTable);
        }
    }

    /// <summary>
    /// 该方法可以找到动态类型对应的表
    /// </summary>
    /// <param name="typeKey"></param>
    /// <returns></returns>
    static DataTable getDataTable(String typeKey) {
        DataTable value = _typeTables.get(typeKey);
        if (value == null)
            throw new DomainDrivenException(String.format("codeart.ddd", "NotFoundDerivedType", typeKey));
        return value;

    }

//	#endregion

    /**
     * 获取对象内部的原始数据
     *
     * @param obj
     * @return
     */
    public static MapData getOriginalData(Object obj) {
        var proxy = ((DomainObject) obj).dataProxy();

        return ((DataProxyImpl) proxy).originalData();
    }

    private final static Map<Class<?>, DbType> _typeMap = new HashMap<Class<?>, DbType>();

    static {

        _typeMap.put(byte.class, DbType.Byte);
        _typeMap.put(Byte.class, DbType.Byte);

        _typeMap.put(short.class, DbType.Int16);
        _typeMap.put(Short.class, DbType.Int16);

        _typeMap.put(int.class, DbType.Int32);
        _typeMap.put(Integer.class, DbType.Int32);

        _typeMap.put(long.class, DbType.Int64);
        _typeMap.put(Long.class, DbType.Int64);

        _typeMap.put(float.class, DbType.Float);
        _typeMap.put(Float.class, DbType.Float);

        _typeMap.put(double.class, DbType.Double);
        _typeMap.put(Double.class, DbType.Double);

        _typeMap.put(boolean.class, DbType.Boolean);
        _typeMap.put(Boolean.class, DbType.Boolean);

        _typeMap.put(String.class, DbType.String);

        _typeMap.put(UUID.class, DbType.Guid);
        _typeMap.put(LocalDateTime.class, DbType.LocalDateTime);
        _typeMap.put(ZonedDateTime.class, DbType.ZonedDateTime);
    }

    /**
     * 获得外键名称
     *
     * @param name 属性名称
     * @return
     */
    public static String getIdName(String name) {
        return _getIdName.apply(name);
    }

    public static String getNameWithSeparated(String name) {
        return _getNameWithSeparated.apply(name);
    }

    public static String getNextName(String name) {
        return _getNextName.apply(name);
    }

    private static Function<String, String> _getIdName = LazyIndexer.init((name) -> {
        return String.format("%s%s", name, EntityObject.IdPropertyName);
    });

    private static Function<String, String> _getNameWithSeparated = LazyIndexer.init((name) -> {
        return String.format("%s_", name);
    });

    private static Function<String, String> _getNextName = LazyIndexer.init((name) -> {
        var pos = name.indexOf("_");
        return name.substring(pos + 1);
    });

    public static Iterable<IDataField> getObjectFields(Class<? extends IAggregateRoot> rootType) {
        return getObjectFields(rootType, rootType);
    }

    public static Iterable<IDataField> getObjectFields(Class<? extends IAggregateRoot> rootType, Class<?> memberType) {
        var mapper = DataMapperFactory.create(rootType);
        var memberMeta = ObjectMetaLoader.get(memberType);
        return mapper.getObjectFields(memberMeta);
    }

}
