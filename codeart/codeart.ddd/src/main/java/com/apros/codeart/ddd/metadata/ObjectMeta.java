package com.apros.codeart.ddd.metadata;

import java.util.ArrayList;

import com.apros.codeart.ddd.DomainDrivenException;
import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.IDynamicObject;
import com.apros.codeart.ddd.IEntityObject;
import com.apros.codeart.ddd.IValueObject;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.util.ListUtil;

public class ObjectMeta {

	private DomainObjectCategory _category;

	public DomainObjectCategory category() {
		return _category;
	}

	private String _name;

	public String name() {
		return _name;
	}

	private Class<?> _objectType;

	/**
	 * 
	 * 类型定义对应的实例类型（也就是真正需要创建的、实际存在内存中的对象类型）
	 * 
	 * @return
	 */
	public Class<?> objectType() {
		return _objectType;
	}

	private ArrayList<PropertyMeta> _properties;

	public Iterable<PropertyMeta> properties() {
		return _properties;
	}

	public PropertyMeta findProperty(String propertyName) {
		var dp = ListUtil.find(_properties, (p) -> p.name().equalsIgnoreCase(propertyName));
		if (dp == null)
			throw new DomainDrivenException(Language.strings("NotFoundDomainProperty", _name, propertyName));
		return dp;
	}

	void addProperty(PropertyMeta property) {
		if (findProperty(property.name()) != null)
			throw new DomainDrivenException(Language.strings("RepeatedDomainProperty", _name, property.name()));
		_properties.add(property);

	}

	ObjectMeta(String name, Class<?> objectType, DomainObjectCategory category) {
		_name = name;
		_objectType = objectType;
		_category = category;
		_properties = new ArrayList<PropertyMeta>();
	}

	// #region 辅助

	final static Class<?> ValueObjectType = IValueObject.class;
	final static Class<?> AggregateRootType = IAggregateRoot.class;
	final static Class<?> EntityObjectType = IEntityObject.class;
	final static Class<?> DomainObjectType = IDomainObject.class;
	final static Class<?> DynamicObjectType = IDynamicObject.class;

	public static boolean isDomainObject(Class<?> type) {
		return type.isAssignableFrom(DomainObjectType);
	}

	public static boolean isValueObject(Class<?> type) {
		return type.isAssignableFrom(ValueObjectType);
	}

	public static boolean isAggregateRoot(Class<?> type) {
		return type.isAssignableFrom(AggregateRootType);
	}

	public static boolean isEntityObject(Class<?> type) {
		return type.isAssignableFrom(EntityObjectType);
	}

	public static boolean isDynamicObject(Class<?> type) {
		return type.isAssignableFrom(DynamicObjectType);
	}

	// #endregion

}
