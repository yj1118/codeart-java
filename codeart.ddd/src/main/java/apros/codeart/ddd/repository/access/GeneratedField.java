package apros.codeart.ddd.repository.access;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.metadata.PropertyAccessLevel;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.metadata.ValueMeta;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.access.internal.AccessUtil;
import apros.codeart.ddd.validation.TimePrecisions;
import apros.codeart.util.ListUtil;

public class GeneratedField extends ValueField {

    @Override
    public DataFieldType fieldType() {
        return DataFieldType.GeneratedField;
    }

    private final GeneratedFieldType _generatedFieldType;

    public GeneratedFieldType generatedFieldType() {
        return _generatedFieldType;
    }

    public GeneratedField(PropertyMeta tip, String name, GeneratedFieldType generatedFieldType,
                          DbFieldType... dbFieldTypes) {
        super(tip, dbFieldTypes);
        this.name(name);
        _generatedFieldType = generatedFieldType;
    }

    public static GeneratedField createValueObjectPrimaryKey(Class<?> reflectedType) {
        var tip = new GuidMeta(EntityObject.IdPropertyName, reflectedType);

        return new GeneratedField(tip, EntityObject.IdPropertyName, GeneratedFieldType.ValueObjectPrimaryKey,
                DbFieldType.PrimaryKey);
    }

    /**
     * 创建引用次数的键
     *
     * @param reflectedType
     * @return
     */
    public static GeneratedField createAssociatedCount(Class<?> reflectedType) {
        var tip = new IntMeta(AssociatedCountName, reflectedType);

        return new GeneratedField(tip, AssociatedCountName, GeneratedFieldType.AssociatedCount, DbFieldType.Common);
    }

    /**
     * 领域类型的编号字段
     *
     * @param reflectedType
     * @return
     */
    public static GeneratedField createTypeKey(Class<?> reflectedType) {
        var tip = new StringMeta(TypeKeyName, reflectedType, 50, true);
        return new GeneratedField(tip, TypeKeyName, GeneratedFieldType.TypeKey, DbFieldType.Common);
    }

    /**
     * 版本号字段
     *
     * @param reflectedType
     * @return
     */
    public static GeneratedField createDataVersion(Class<?> reflectedType) {
        var tip = new IntMeta(DataVersionName, reflectedType);
        return new GeneratedField(tip, DataVersionName, GeneratedFieldType.DataVersion, DbFieldType.Common);
    }

    /// <summary>
    /// 创建中间表多个数据的排序序号键
    /// </summary>
    /// <param name="reflectedType"></param>
    /// <returns></returns>
    public static GeneratedField createOrderIndex(Class<?> reflectedType, DbFieldType... types) {
        var tip = new IntMeta(OrderIndexName, reflectedType);
        return new GeneratedField(tip, OrderIndexName, GeneratedFieldType.Index, types);
    }

    public static GeneratedField create(String name, Class<?> propertyType, Class<?> declaringType) {
        var tip = new CustomMeta(name, propertyType, declaringType);
        return new GeneratedField(tip, name, GeneratedFieldType.User);
    }

    /**
     * 创建基础值集合的值字段
     *
     * @param declaringType
     * @param field
     * @return
     */
    public static GeneratedField createPrimitiveValue(Class<?> declaringType, ValueListField field) {
        var valueType = field.valueType();
        var agent = DataSource.getAgent();
        PropertyMeta tip = null;
        DbFieldType fieldType = DbFieldType.Common;
        if (valueType.equals(String.class)) {
            var maxLength = AccessUtil.getMaxLength(field.tip());

            // 如果value的字符串类型满足数据库要求，那么就可以参与索引
            if (maxLength < agent.getStringIndexableMaxLength()) {
                fieldType = DbFieldType.NonclusteredIndex;
            }
            var ascii = AccessUtil.isASCIIString(field.tip());
            tip = new StringMeta(PrimitiveValueName, declaringType, maxLength, ascii);
        } else if (valueType.equals(LocalDateTime.class)) {
            var precision = AccessUtil.getTimePrecision(field.tip());

            fieldType = DbFieldType.NonclusteredIndex;
            tip = new DateTimeMeta(PrimitiveValueName, declaringType, precision);
        } else {
            fieldType = DbFieldType.NonclusteredIndex;
            tip = new CustomMeta(PrimitiveValueName, valueType, declaringType);
        }

        return new GeneratedField(tip, PrimitiveValueName, GeneratedFieldType.PrimitiveValue, fieldType);
    }

    public static GeneratedField createString(Class<?> declaringType, String name, int maxLength, boolean ascii) {
        var tip = new StringMeta(name, declaringType, maxLength, ascii);
        return new GeneratedField(tip, name, GeneratedFieldType.User);
    }

    public static final String AssociatedCountName = "AssociatedCount";

    public static final String OrderIndexName = "OrderIndex";
    public static final String DataVersionName = "DataVersion";
    public static final String TypeKeyName = "TypeKey";

    public static final String RootIdName = "RootId";
    public static final String MasterIdName = "MasterId";
    public static final String SlaveIdName = "SlaveId";
    public static final String PrimitiveValueName = "Value";

    public static class CustomMeta extends PropertyMeta {

        public CustomMeta(String name, Class<?> propertyType, Class<?> declaringType) {
            super(name, ValueMeta.createBy(propertyType), ObjectMetaLoader.get(declaringType),
                    PropertyAccessLevel.Public, PropertyAccessLevel.Public, null, ListUtil.empty(), false, null);
        }
    }

    public static class GuidMeta extends CustomMeta {

        public GuidMeta(String name, Class<?> declaringType) {
            super(name, UUID.class, declaringType);
        }
    }

    public static class IntMeta extends CustomMeta {

        public IntMeta(String name, Class<?> declaringType) {
            super(name, int.class, declaringType);
        }
    }

    public static class StringMeta extends CustomMeta {

        private final int _maxLength;

        public int maxLength() {
            return _maxLength;
        }

        private final boolean _ascii;

        public boolean ascii() {
            return _ascii;
        }

        public StringMeta(String name, Class<?> declaringType, int maxLength, boolean ascii) {
            super(name, String.class, declaringType);
            _maxLength = maxLength;
            _ascii = ascii;
        }
    }

    public static class DateTimeMeta extends CustomMeta {

        private final TimePrecisions _precision;

        public TimePrecisions precision() {
            return _precision;
        }

        public DateTimeMeta(String name, Class<?> declaringType, TimePrecisions precision) {
            super(name, String.class, declaringType);
            _precision = precision;
        }
    }

    public static class NumericMeta extends CustomMeta {

        private final int _precision;

        public int precision() {
            return _precision;
        }

        private final int _scale;

        public int scale() {
            return _scale;
        }

        public NumericMeta(String name, Class<?> declaringType, int precision, int scale) {
            super(name, BigDecimal.class, declaringType);
            _precision = precision;
            _scale = scale;
        }
    }

}
