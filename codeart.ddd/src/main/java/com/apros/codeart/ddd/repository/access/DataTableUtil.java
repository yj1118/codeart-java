package com.apros.codeart.ddd.repository.access;

import static com.apros.codeart.i18n.Language.strings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.apros.codeart.ddd.Emptyable;
import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.ddd.validation.ASCIIStringValidator;
import com.apros.codeart.ddd.validation.StringLengthValidator;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.EnumUtil;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.LazyIndexer;
import com.google.common.collect.Iterables;

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
	 * 
	 * 将从对象里提取的字段转换为数据库表的字段
	 * 
	 * @param objectFields
	 * @return
	 */
	public static Iterable<IDataField> getTableFields(Iterable<IDataField> objectFields) {
		ArrayList<IDataField> fields = new ArrayList<IDataField>();
		for (var objectField : objectFields)
			fillFields(fields, objectField);
		return fields;
	}

	private static void fillFields(ArrayList<IDataField> fields, IDataField current) {
		String name = current.name();
		switch (current.fieldType()) {
		case DataFieldType.GeneratedField: {
			fields.add(current); // 对于生成的键，直接追加
		}
		case DataFieldType.Value: {
			var valueField = TypeUtil.as(current, ValueField.class);
			// 存值
			var field = new ValueField(current.tip(), Iterables.toArray(valueField.dbFieldTypes(), DbFieldType.class));

			field.name(name);
			field.parentMemberField(current.parentMemberField());

			fields.add(field);
		}
		case DataFieldType.EntityObject:
		case DataFieldType.AggregateRoot: {
			// 存外键即可
			var idTip = PropertyMeta.getProperty(current.tip().monotype(), EntityObject.IdPropertyName);

			var field = new ValueField(idTip);
			field.name(getIdName(name));
			field.parentMemberField(current);

			fields.add(field);
		}
		case DataFieldType.ValueObject: {
			var primaryKey = GeneratedField.createValueObjectPrimaryKey(current.tip().monotype());
			var field = new ValueField(primaryKey.tip());
			field.name(getIdName(name));
			field.parentMemberField(current);

			fields.add(field);
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
		if (dbType == null)
			return dbType;

		throw new IllegalStateException(strings("codeart.ddd", "DataTypeNotSupported", dataType.getName()));
	}

	public static int getMaxLength(PropertyMeta meta) {
		var stringMeta = TypeUtil.as(meta, GeneratedField.StringMeta.class);
		if (stringMeta != null) {
			return stringMeta.maxLength();
		} else {
			var sl = meta.findValidator(StringLengthValidator.class);
			return sl == null ? 0 : sl.max();
		}
	}

	public static boolean isASCIIString(PropertyMeta meta) {
		var stringMeta = TypeUtil.as(meta, GeneratedField.StringMeta.class);
		if (stringMeta != null) {
			return stringMeta.ascii();
		} else {
			return meta.findValidator(ASCIIStringValidator.class) != null;
		}
	}

	public static ValueField getForeignKey(DataTable table, GeneratedFieldType keyType, DbFieldType... dbFieldTypes) {
		if (table.idField() == null)
			throw new IllegalStateException(Language.strings("codeart.ddd", "TableNotId", table.name()));
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
		_typeMap.put(LocalDateTime.class, DbType.DateTime);
	}

	/**
	 * 
	 * 获得外键名称
	 * 
	 * @param name 属性名称
	 * @return
	 */
	public static String getIdName(String name) {
		return _getIdName.apply(name);
	}

	private static Function<String, String> _getIdName = LazyIndexer.init((name) -> {
		return String.format("%s%s", name, EntityObject.IdPropertyName);
	});

	public static Iterable<IDataField> getObjectFields(Class<?> objectType) {
		var objectMeta = ObjectMetaLoader.get(objectType);
		var mapper = DataMapperFactory.create(objectMeta);
		return mapper.getObjectFields(objectMeta);
	}
}
