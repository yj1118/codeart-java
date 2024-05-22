package apros.codeart.ddd.metadata;

import java.util.ArrayList;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.FrameworkDomain;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.IEntityObject;
import apros.codeart.ddd.IObjectValidator;
import apros.codeart.ddd.IValueObject;
import apros.codeart.ddd.MergeDomain;
import apros.codeart.ddd.dynamic.IDynamicObject;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;

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
			throw new DomainDrivenException(
					Language.strings("codeart.ddd", "NotFoundDomainProperty", _name, propertyName));
		return dp;
	}

	void addProperty(PropertyMeta property) {
		if (findProperty(property.name()) != null)
			throw new DomainDrivenException(
					Language.strings("codeart.ddd", "RepeatedDomainProperty", _name, property.name()));
		_properties.add(property);

	}

	private Iterable<IObjectValidator> _validators;

	public Iterable<IObjectValidator> validators() {
		return _validators;
	}

	private ObjectRepositoryTip _repositoryTip;

	public ObjectRepositoryTip repositoryTip() {
		return _repositoryTip;
	}

	public ObjectMeta(String name, Class<?> objectType, DomainObjectCategory category,
			Iterable<IObjectValidator> validators, ObjectRepositoryTip repositoryTip) {
		_name = name;
		_objectType = objectType;
		_category = category;
		_properties = new ArrayList<PropertyMeta>();
		_validators = validators;
		_repositoryTip = repositoryTip;
	}

	public DTObject toDTO() {
		return SchemeCode.get(this);
	}

	public DTObject toDTO(Iterable<String> propertyNames) {
		return SchemeCode.get(this, propertyNames);
	}

	@Override
	public boolean equals(Object obj) {
		var target = TypeUtil.as(obj, ObjectMeta.class);
		if (target == null)
			return false;
		return this.objectType().equals(target.objectType());
	}

	@Override
	public int hashCode() {
		return _objectType.hashCode();
	}

	// #region 辅助

	final static Class<?> ValueObjectType = IValueObject.class;
	final static Class<?> AggregateRootType = IAggregateRoot.class;
	final static Class<?> EntityObjectType = IEntityObject.class;
	final static Class<?> DomainObjectType = IDomainObject.class;
	final static Class<?> DynamicObjectType = IDynamicObject.class;

	public static boolean isDomainObject(Class<?> type) {
		return DomainObjectType.isAssignableFrom(type);
	}

	public static boolean isValueObject(Class<?> type) {
		return ValueObjectType.isAssignableFrom(type);
	}

	public static boolean isAggregateRoot(Class<?> type) {
		return AggregateRootType.isAssignableFrom(type);
	}

	public static boolean isEntityObject(Class<?> type) {
		return EntityObjectType.isAssignableFrom(type);
	}

	public static boolean isDynamicObject(Class<?> type) {
		return DynamicObjectType.isAssignableFrom(type);
	}

	public static boolean isFrameworkDomainType(Class<?> objectType) {
		if (isDynamicObject(objectType))
			return true;

		// 因为框架提供的基类没有标记ObjectRepositoryAttribute
		return isDomainObject(objectType) && TypeUtil.isDefined(objectType, FrameworkDomain.class);
	}

	public static boolean isMergeDomainType(Class<?> objectType) {
		return TypeUtil.isDefined(objectType, MergeDomain.class);
	}

	// #endregion

}
