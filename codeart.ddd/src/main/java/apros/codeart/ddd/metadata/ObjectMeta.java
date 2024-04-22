package apros.codeart.ddd.metadata;

import java.util.ArrayList;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.FrameworkDomain;
import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.IDynamicObject;
import apros.codeart.ddd.IEntityObject;
import apros.codeart.ddd.IObjectValidator;
import apros.codeart.ddd.IValueObject;
import apros.codeart.ddd.MergeDomain;
import apros.codeart.ddd.dynamic.internal.TypeDefine;
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

	private boolean _remotable;

	/**
	 * 对象是否具有远程的能力
	 * 
	 * @return
	 */
	public boolean remotable() {
		return _remotable;
	}

	private ObjectRepositoryTip _repositoryTip;

	public ObjectRepositoryTip repositoryTip() {
		return _repositoryTip;
	}

	private TypeDefine _define;

	/**
	 * 
	 * 类型定义，只有动态类型才有此定义
	 * 
	 * @return
	 */
	public TypeDefine getDefine() {
		return _define;
	}

	public TypeDefine setDefine(TypeDefine define) {
		return _define;
	}

	/**
	 * 
	 * 是否为注入或者声明的动态类型
	 * 
	 * @return
	 */
	public boolean isDynamic() {
		return _define != null;
	}

	private String _schemeCode;

	public String schemeCode() {
		return _schemeCode;
	}

	private String getSchemeCode() {
		// todo
		return "";
	}

	ObjectMeta(String name, Class<?> objectType, DomainObjectCategory category, Iterable<IObjectValidator> validators,
			boolean remotable, ObjectRepositoryTip repositoryTip) {
		_name = name;
		_objectType = objectType;
		_category = category;
		_properties = new ArrayList<PropertyMeta>();
		_validators = validators;
		_remotable = remotable;
		_repositoryTip = repositoryTip;
		_schemeCode = getSchemeCode();
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
