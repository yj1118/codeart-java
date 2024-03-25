package com.apros.codeart.ddd.metadata;

import java.util.function.BiFunction;

import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.ddd.DomainProperty;
import com.apros.codeart.runtime.TypeUtil;

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
	private ValueMeta _value;

	public ValueMeta value() {
		return _value;
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

	public PropertyMeta(String name, ValueMeta value, ObjectMeta declaring, PropertyAccessLevel accessGet,
			PropertyAccessLevel accessSet, String call) {
		_name = name;
		_value = value;
		_declaring = declaring;
		_category = getCategory(value);
		_accessGet = accessGet;
		_accessSet = accessSet;
		_call = call; // call可以为空，空表示没有使用多语言

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

	private static DomainPropertyCategory getCategory(ValueMeta valueMeta) {

		if (valueMeta.isList()) {

			var elementType = valueMeta.monotype();
			if (ObjectMeta.isAggregateRoot(elementType))
				return DomainPropertyCategory.AggregateRootList;
			if (ObjectMeta.isEntityObject(elementType))
				return DomainPropertyCategory.EntityObjectList;
			if (ObjectMeta.isValueObject(elementType))
				return DomainPropertyCategory.ValueObjectList;
			return DomainPropertyCategory.PrimitiveList;

		} else {

			var objectType = valueMeta.monotype();
			if (ObjectMeta.isAggregateRoot(objectType))
				return DomainPropertyCategory.AggregateRoot;
			if (ObjectMeta.isEntityObject(objectType))
				return DomainPropertyCategory.EntityObject;
			if (ObjectMeta.isValueObject(objectType))
				return DomainPropertyCategory.ValueObject;
			return DomainPropertyCategory.Primitive;
		}

	}

	static PropertyMeta getProperty(Class<?> doType, String propertyName) {
		var obj = ObjectMetaLoader.get(propertyName);
		return obj.findProperty(propertyName);
	}
}
