package com.apros.codeart.ddd.repository.access;

import static com.apros.codeart.i18n.Language.strings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.apros.codeart.ddd.Emptyable;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.runtime.EnumUtil;
import com.apros.codeart.runtime.FieldUtil;
import com.apros.codeart.util.StringUtil;

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
		for (var i = 0; i < objectFields.Count; i++) {
			var objectField = objectFields[i];
			fillFields(fields, objectField);
		}
		return fields;
	}

	private static void fillFields(List<IDataField> fields, IDataField current) {
		String name=current.name();switch(current.FieldType){case DataFieldType.GeneratedField:{fields.Add(current); // 对于生成的键，直接追加
		}case DataFieldType.Value:{var valueField=current as ValueField;
		// 存值
		var field=new ValueField(current.Tip,valueField.DbFieldTypes.ToArray()){Name=name,ParentMemberField=current.ParentMemberField};fields.Add(field);return true;}case DataFieldType.EntityObject:case DataFieldType.EntityObjectPro:case DataFieldType.AggregateRoot:{
		// 存外键即可
		var idAttr=DomainProperty.GetProperty(current.Tip.PropertyType,EntityObject.IdPropertyName).RepositoryTip;

		var field=new ValueField(idAttr){Name=_getIdName(name),ParentMemberField=current};fields.Add(field);return true;}case DataFieldType.ValueObject:{var primaryKey=GeneratedField.CreateValueObjectPrimaryKey(current.Tip.PropertyType);var field=new ValueField(primaryKey.Tip){Name=_getIdName(name),ParentMemberField=current};fields.Add(field);return true;
	}default:

	{
		break;
	}
	}return false;}

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

}
