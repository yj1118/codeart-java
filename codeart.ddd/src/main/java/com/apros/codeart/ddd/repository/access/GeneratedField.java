package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.ddd.metadata.PropertyAccessLevel;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.ddd.metadata.ValueMeta;
import com.apros.codeart.util.Guid;
import com.apros.codeart.util.ListUtil;

class GeneratedField extends ValueField {

	@Override
	public DataFieldType fieldType() {
		return DataFieldType.GeneratedField;
	}

	private GeneratedFieldType _generatedFieldType;

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
	 * 
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
	 * 
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
	public static GeneratedField CreateOrderIndex(Class<?> reflectedType, DbFieldType... types) {
		var tip = new IntMeta(OrderIndexName, reflectedType);
		return new GeneratedField(tip, OrderIndexName, GeneratedFieldType.Index, types);
	}

	public static GeneratedField create(String name, Class<?> propertyType, Class<?> declaringType) {
		var tip = new CustomMeta(name, propertyType, declaringType);
		return new GeneratedField(tip, name, GeneratedFieldType.User);
	}

	/// <summary>
	/// 创建基础值集合的值字段
	/// </summary>
	/// <param name="ownerType"></param>
	/// <param name="propertyType"></param>
	/// <param name="name"></param>
	/// <returns></returns>
	public static GeneratedField createPrimitiveValue(Class<?> declaringType, ValueListField field) {
		var valueType = field.valueType();
		var agent = DataSource.getAgent();
		PropertyMeta tip = null;
		DbFieldType fieldType = DbFieldType.Common;
		if (valueType.equals(String.class)) {
			var maxLength = DataTableUtil.getMaxLength(field.tip());

			// 如果value的字符串类型满足数据库要求，那么就可以参与索引
			if (maxLength < agent.getStringIndexableMaxLength()) {
				fieldType = DbFieldType.NonclusteredIndex;
			}
			var ascii = DataTableUtil.isASCIIString(field.tip());
			tip = new StringMeta(PrimitiveValueName, declaringType, maxLength, ascii);
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
	public static final String TenantIdName = "TenantId";

	public static class CustomMeta extends PropertyMeta {

		public CustomMeta(String name, Class<?> propertyType, Class<?> declaringType) {
			super(name, ValueMeta.createBy(propertyType), ObjectMetaLoader.get(declaringType),
					PropertyAccessLevel.Public, PropertyAccessLevel.Public, null, ListUtil.empty(), false, null);
		}
	}

	public static class GuidMeta extends CustomMeta {

		public GuidMeta(String name, Class<?> declaringType) {
			super(name, Guid.class, declaringType);
		}
	}

	public static class IntMeta extends CustomMeta {

		public IntMeta(String name, Class<?> declaringType) {
			super(name, int.class, declaringType);
		}
	}

	public static class StringMeta extends CustomMeta {

		private int _maxLength;

		public int maxLength() {
			return _maxLength;
		}

		private boolean _ascii;

		public boolean ascii() {
			return _ascii;
		}

		public StringMeta(String name, Class<?> declaringType, int maxLength, boolean ascii) {
			super(name, String.class, declaringType);
			_maxLength = maxLength;
			_ascii = ascii;
		}
	}

}
