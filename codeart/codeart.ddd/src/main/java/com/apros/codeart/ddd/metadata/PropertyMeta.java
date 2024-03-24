package com.apros.codeart.ddd.metadata;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.DomainProperty;
import com.apros.codeart.ddd.Emptyable;
import com.apros.codeart.ddd.IEmptyable;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.Guid;
import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.StringUtil;

public class PropertyMeta {

	private String _id;

	public String id() {
		return _id;
	}

	private String _name;

	/**
	 * 属性名称
	 * 
	 * @return
	 */
	public String name() {
		return _name;
	}

	/**
	 * 属性的值的类型，比如字符串/整型等
	 */
	private Class<?> _type;

	public Class<?> Type() {
		return _type;
	}

	private ObjectMeta _declaring;

	public ObjectMeta declaring() {
		return _declaring;
	}

	private DomainPropertyCategory _category;

	public DomainPropertyCategory category() {
		return _category;
	}

	private PropertyAccessLevel _accessGet;

	public PropertyAccessLevel accessGet() {
		return _accessGet;
	}

	private PropertyAccessLevel _accessSet;

	public PropertyAccessLevel accessSet() {
		return _accessSet;
	}

	public boolean isPublicSet() {
		return _accessSet == PropertyAccessLevel.Public;
	}

	private String _call;

	/**
	 * 称呼
	 * 
	 * @return
	 */
	public String call() {
		return _call;
	}

	private BiFunction<DomainObject, DomainProperty, Object> _getDefaultValue;

	public BiFunction<DomainObject, DomainProperty, Object> getDefaultValue() {
		return _getDefaultValue;
	}

	/**
	 * 属性是否引用的是内聚根（或内聚根集合）
	 * 
	 * @return
	 */
	public boolean isQuoteAggreateRoot() {
		return _category == DomainPropertyCategory.AggregateRoot
				|| _category == DomainPropertyCategory.AggregateRootList;
	}

	public PropertyMeta(String name, Class<?> type, ObjectMeta declaring, PropertyAccessLevel accessGet,
			PropertyAccessLevel accessSet, String call,
			BiFunction<DomainObject, DomainProperty, Object> getDefaultValue) {
		_name = name;
		_type = type;
		_declaring = declaring;
		_category = getCategory(type);
		_accessGet = accessGet;
		_accessSet = accessSet;
		_call = call; // call可以为空，空表示没有使用多语言
		_getDefaultValue = getDefaultValue == null ? (obj, pro) -> {
			return detectDefaultValue(type);
		} : getDefaultValue;

		declaring.addProperty(this); // 关联
	}

	@Override
	public boolean equals(Object obj) {
		var target = TypeUtil.as(obj, PropertyMeta.class);
		if (target == null)
			return false;
		return this.id() == target.id();
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}

	private static DomainPropertyCategory getCategory(Class<?> propertyType) {

		if (TypeUtil.isCollection(propertyType)) {

			// 原始代码中是要传递 dynamicType的，做了以下判断，但是新版认为，动态类型本身救应该被兼容，这块需要升级设计
			// todo
			// var elementType = dynamicType != null ? dynamicType :
			// TypeUtil.resolveElementType(propertyType);

			var elementType = TypeUtil.resolveElementType(propertyType);

			if (ObjectMeta.isAggregateRoot(elementType))
				return DomainPropertyCategory.AggregateRootList;
			if (ObjectMeta.isEntityObject(elementType))
				return DomainPropertyCategory.EntityObjectList;
			if (ObjectMeta.isValueObject(elementType))
				return DomainPropertyCategory.ValueObjectList;
			return DomainPropertyCategory.PrimitiveList;
		} else {

			if (ObjectMeta.isAggregateRoot(propertyType))
				return DomainPropertyCategory.AggregateRoot;
			if (ObjectMeta.isEntityObject(propertyType))
				return DomainPropertyCategory.EntityObject;
			if (ObjectMeta.isValueObject(propertyType))
				return DomainPropertyCategory.ValueObject;
			return DomainPropertyCategory.Primitive;
		}

	}

	private static Object detectDefaultValue(Class<?> propertyValueClass) {
		return _detectDefaultValue.apply(propertyValueClass);
	}

	private static Function<Class<?>, Object> _detectDefaultValue = LazyIndexer.init((type) -> {
		if (type.equals(String.class))
			return StringUtil.empty();
		if (ObjectMeta.isDomainObject(type)) {
			return DomainObject.getEmpty(type);
		}
		return getDefaultValue(type);
	});

	public static Object getDefaultValue(Class<?> valueType) {
		if (valueType.equals(String.class))
			return StringUtil.empty();

		if (valueType.equals(UUID.class))
			return Guid.Empty;

		if (valueType.equals(LocalDate.class))
			return LocalDate.MIN;

		if (valueType.equals(int.class) || valueType.equals(Integer.class) || valueType.equals(long.class)
				|| valueType.equals(byte.class) || valueType.equals(Byte.class) || valueType.equals(Long.class)
				|| valueType.equals(float.class) || valueType.equals(Float.class) || valueType.equals(double.class)
				|| valueType.equals(Double.class) || valueType.equals(short.class) || valueType.equals(Short.class))
			return 0;

		if (valueType.isAssignableFrom(IEmptyable.class))
			return Emptyable.createEmpty();

		if (valueType.equals(char.class))
			return StringUtil.empty();

		return null;
	}

}
