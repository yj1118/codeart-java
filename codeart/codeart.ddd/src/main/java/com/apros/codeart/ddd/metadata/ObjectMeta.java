package com.apros.codeart.ddd.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.apros.codeart.ddd.DomainDrivenException;
import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.IDynamicObject;
import com.apros.codeart.ddd.IEntityObject;
import com.apros.codeart.ddd.IValueObject;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.util.ListUtil;

public class ObjectMeta {

	private String _name;

	public String name() {
		return _name;
	}

	private ArrayList<PropertyMeta> _properties;

	public Iterable<PropertyMeta> properties() {
		return _properties;
	}

	public PropertyMeta findProperty(String propertyName) {
		return ListUtil.find(_properties, (p) -> p.name().equalsIgnoreCase(propertyName));
	}

	void addProperty(PropertyMeta property) {
		if (findProperty(property.name()) != null)
			throw new DomainDrivenException(Language.strings("RepeatedDomainProperty", _name, property.name()));
		_properties.add(property);

	}

	public ObjectMeta(String name) {
		_name = name;
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

//			#endregion

	private static Map<String, ObjectMeta> _metas = new HashMap<>();

	public static ObjectMeta obtain(String objectName) {
		var meta = _metas.get(objectName);
		if (meta == null) {
			synchronized (_metas) {
				meta = _metas.get(objectName);
				if (meta == null) {
					meta = new ObjectMeta(objectName);
					_metas.put(objectName, meta);
				}
			}
		}
		return meta;
	}

	/**
	 * 
	 * 由于是初始化期间执行的obtain，初始化完毕后，才会执行get，所以不会有并发问题
	 * 
	 * @param objectName
	 * @return
	 */
	public static ObjectMeta get(String objectName) {
		return _metas.get(objectName);
	}

	public static ObjectMeta obtain(Class<?> objectType) {
		return obtain(objectType.getSimpleName());
	}

}
