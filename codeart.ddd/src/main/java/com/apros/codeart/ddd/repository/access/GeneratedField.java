package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.IPropertyDataLoader;
import com.apros.codeart.ddd.IPropertyValidator;
import com.apros.codeart.ddd.metadata.ObjectMeta;
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

	/// <summary>
	/// 创建引用次数的键
	/// </summary>
	/// <param name="reflectedType"></param>
	/// <returns></returns>
	public static GeneratedField CreateAssociatedCount(Type reflectedType)
	{
	    var attr = new PropertyRepositoryAttribute()
	    {
	        Property = new IntProperty(reflectedType,AssociatedCountName)
	    };
	    return new GeneratedField(attr, AssociatedCountName, GeneratedFieldType.AssociatedCount, DbFieldType.Common);
	}

	/// <summary>
	/// 领域类型的编号字段
	/// </summary>
	/// <param name="reflectedType"></param>
	/// <returns></returns>
	public static GeneratedField CreateTypeKey(Type reflectedType)
	{
	    var attr = new PropertyRepositoryAttribute()
	    {
	        Property = new StringProperty(reflectedType, TypeKeyName, 50, true)
	    };
	    return new GeneratedField(attr, TypeKeyName, GeneratedFieldType.TypeKey, DbFieldType.Common);
	}

	/// <summary>
	/// 更新时间的字段
	/// </summary>
	/// <param name="reflectedType"></param>
	/// <returns></returns>
	public static GeneratedField CreateDataVersion(Type reflectedType)
	{
	    var attr = new PropertyRepositoryAttribute()
	    {
	        Property = new IntProperty(reflectedType, DataVersionName)
	    };
	    return new GeneratedField(attr, DataVersionName, GeneratedFieldType.DataVersion, DbFieldType.Common);
	}

	public const

	string AssociatedCountName = "AssociatedCount";public const
	string OrderIndexName = "OrderIndex";public const
	string DataVersionName = "DataVersion";public const
	string TypeKeyName = "TypeKey";

	public const
	string RootIdName = "RootId";public const
	string MasterIdName = "MasterId";public const
	string SlaveIdName = "SlaveId";public const
	string PrimitiveValueName = "Value";public const
	string TenantIdName = "TenantId";

	/// <summary>
	/// 创建中间表多个数据的排序序号键
	/// </summary>
	/// <param name="reflectedType"></param>
	/// <returns></returns>
	public static GeneratedField CreateOrderIndex(Type reflectedType, params DbFieldType[] types)
	{
	    var attr = new PropertyRepositoryAttribute()
	    {
	        Property = new IntProperty(reflectedType, OrderIndexName)
	    };
	    return new GeneratedField(attr, OrderIndexName, GeneratedFieldType.Index, types);
	}

	public static GeneratedField Create(Type ownerType, Type propertyType, string name)
	{
	    var attr = new PropertyRepositoryAttribute()
	    {
	        Property = new CustomProperty(ownerType, propertyType, name)
	    };
	    return new GeneratedField(attr, name, GeneratedFieldType.User);
	}

	/// <summary>
	/// 创建基础值集合的值字段
	/// </summary>
	/// <param name="ownerType"></param>
	/// <param name="propertyType"></param>
	/// <param name="name"></param>
	/// <returns></returns>
	public static GeneratedField CreatePrimitiveValue(Type ownerType, ValueListField field)
	{
	    var valueType = field.ValueType;
	    DomainProperty property = null;
	    DbFieldType fieldType = DbFieldType.Common;
	    if (valueType == typeof(string))
	    {
	        var maxLength = field.Tip.GetMaxLength();
	        if(maxLength < 300)
	        {
	            //如果value的字符串类型长度小于300，那么就可以参与索引
	            fieldType = DbFieldType.NonclusteredIndex;
	        }
	        property = new StringProperty(ownerType, PrimitiveValueName, maxLength, field.Tip.IsASCIIString());
	    }
	    else
	    {
	        fieldType = DbFieldType.NonclusteredIndex;
	        property = new CustomProperty(ownerType, valueType, PrimitiveValueName);
	    }

	    var attr = new PropertyRepositoryAttribute()
	    {
	        Property = property
	    };
	    return new GeneratedField(attr, PrimitiveValueName, GeneratedFieldType.PrimitiveValue, fieldType);
	}

	public static GeneratedField CreateString(Type ownerType, string name, int maxLength, bool ascii)
	{
	    var attr = new PropertyRepositoryAttribute()
	    {
	        Property = new StringProperty(ownerType, name, maxLength, ascii)
	    };
	    return new GeneratedField(attr, name, GeneratedFieldType.User);
	}

	private static class CustomMeta extends PropertyMeta {

		public CustomMeta(String name, Class<?> propertyType, Class<?> declaring) {
			super(name, ValueMeta.createBy(propertyType), ObjectMetaLoader.get(declaring), PropertyAccessLevel.Public,
					PropertyAccessLevel.Public, null, ListUtil.empty(), false, null);
		}
	}

	private class GuidMeta extends CustomMeta {

		public GuidMeta(String name, Class<?> declaring) {
			super(name, Guid.class, declaring);
		}
	}

	private class IntProperty:CustomProperty
	{

		public IntProperty(Type ownerType, string name)
	        : base(ownerType, typeof(int), name)
	    {
	    }
	}

		internal

		class StringProperty:CustomProperty
	{
		public int MaxLength
		{
	        get;
	        private set;
	    }

			public bool IsASCII
		{
	        get;
	        private set;
	    }

		public StringProperty(Type ownerType, string name,int maxLength,bool isASCII)
	        : base(ownerType, typeof(string), name)
	    {
	        this.MaxLength = maxLength;
	        this.IsASCII = isASCII;
	    }
	}
}
