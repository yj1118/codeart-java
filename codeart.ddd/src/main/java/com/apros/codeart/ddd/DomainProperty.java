package com.apros.codeart.ddd;

import static com.apros.codeart.runtime.Util.propagate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.BiFunction;

import com.apros.codeart.ddd.dynamic.DynamicProperty;
import com.apros.codeart.ddd.metadata.DomainPropertyCategory;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.ddd.metadata.PropertyAccessLevel;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.ddd.metadata.ValueMeta;
import com.apros.codeart.ddd.repository.PropertyRepository;
import com.apros.codeart.ddd.repository.PropertyRepositoryImpl;
import com.apros.codeart.runtime.FieldUtil;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.MapList;
import com.apros.codeart.util.StringUtil;
import com.google.common.base.Objects;

/**
 * 注册属性元数据和使用元数据的职责
 * 
 * 但是不负责存放元数据
 * 
 */
public class DomainProperty {

	private PropertyMeta _meta;

	/**
	 * 属性名称
	 * 
	 * @return
	 */
	public String name() {
		return _meta.name();
	}

	public Class<?> monotype() {
		return _meta.monotype();
	}

	public boolean isCollection() {
		return _meta.isCollection();
	}

	PropertyAccessLevel accessSet() {
		return _meta.accessSet();
	}

	PropertyAccessLevel accessGet() {
		return _meta.accessGet();
	}

	/**
	 * 称呼（支持多语言）
	 * 
	 * @return
	 */
	public String call() {
		if (StringUtil.isNullOrEmpty(_meta.call()))
			return this.name();
		return PropertyLabelAnn.getValue(_meta.call());
	}

	/**
	 * 拥有该属性的类型
	 */
	public Class<?> declaringType() {
		return _meta.declaringType();
	}

	public DomainPropertyCategory category() {
		return _meta.category();
	}

	/**
	 * 
	 * 获得属性的默认值,传入属性所在的对象和成员对应的属性定义
	 * 
	 * @param obj
	 * @param property
	 * @return
	 */
	public Object getDefaultValue(DomainObject obj, DomainProperty property) {
		return _meta.getDefaultValue().apply(obj, property);
	}

	/**
	 * 属性验证器
	 * 
	 * @return
	 */
	public Iterable<IPropertyValidator> validators() {
		return _meta.validators();
	}

	/**
	 * 属性是否引用的是内聚根（或内聚根集合）
	 * 
	 * @return
	 */
	public boolean isQuoteAggreateRoot() {
		return _meta.category() == DomainPropertyCategory.AggregateRoot
				|| _meta.category() == DomainPropertyCategory.AggregateRootList;
	}

	boolean isChanged(Object oldValue, Object newValue) {
		// 属性如果是集合、实体对象（引用对象），那么不用判断值，直接认为是被改变了 todo（该算法是否修复成更高效，更精准，稍后琢磨）
		if (this.isCollection() || _meta.category() == DomainPropertyCategory.EntityObject)
			return true;

		// 普通类型就用常规比较
		return !Objects.equal(oldValue, newValue);
	}

	public String id() {
		return _meta.id();
	}

	@Override
	public boolean equals(Object obj) {
		var target = TypeUtil.as(obj, DomainProperty.class);
		if (target == null)
			return false;
		return this.id() == target.id();
	}

	@Override
	public int hashCode() {
		return id().hashCode();
	}

//	#endregion
//
//	#region 属性仓储的定义

	PropertyRepositoryImpl _repositoryTip;

	PropertyRepositoryImpl getRepositoryTip() {
		return _repositoryTip;
	}

	DomainProperty(PropertyMeta meta) {
		_meta = meta;
	}

	private static class AccessInfo {
		public PropertyAccessLevel _accessGet;
		public PropertyAccessLevel _accessSet;

		public PropertyAccessLevel get() {
			return _accessGet;
		}

		public PropertyAccessLevel set() {
			return _accessSet;
		}

		public AccessInfo(PropertyAccessLevel accessGet, PropertyAccessLevel accessSet) {
			_accessGet = accessGet;
			_accessSet = accessSet;
		}
	}

	private static AccessInfo getAccess(String propertyName, Class<?> declaringType) {
		var field = FieldUtil.getField(declaringType, propertyName);

		var accessGet = FieldUtil.canRead(field) ? PropertyAccessLevel.Public : PropertyAccessLevel.Private;
		var accessSet = FieldUtil.canWrite(field) ? PropertyAccessLevel.Public : PropertyAccessLevel.Private;

		return new AccessInfo(accessGet, accessSet);
	}

	private static DomainProperty register(String name, boolean isCollection, Class<?> monotype, Class<?> declaringType,
			BiFunction<DomainObject, DomainProperty, Object> getDefaultValue) {

		// 先获得属性上所有的特性标签
		var anns = getAnnotations(declaringType, name);

		var access = getAccess(name, declaringType);
		var call = getCall(anns);

		var declaring = ObjectMetaLoader.get(declaringType);

		var valueMeta = ValueMeta.createBy(isCollection, monotype, getDefaultValue);

		var validators = PropertyValidatorImpl.getValidators(anns);

		var repositoryTip = getRepository(anns, declaringType);

		var meta = new PropertyMeta(name, valueMeta, declaring, access.get(), access.set(), call, validators,
				repositoryTip.lazy(), repositoryTip.loader());

		var property = new DomainProperty(meta);
		addProperty(property);
		return property;
	}

	public static DomainProperty register(String name, Class<?> propertyType, Class<?> declaringType) {
		return register(name, false, propertyType, declaringType, null);
	}

	public static DomainProperty register(String name, Class<?> propertyType, Class<?> declaringType,
			Object defaultValue) {
		return register(name, false, propertyType, declaringType, (obj, pro) -> {
			return defaultValue;
		});
	}

	/**
	 * 注册集合
	 * 
	 * @param name
	 * @param elementType
	 * @param declaringType
	 * @return
	 */
	public static DomainProperty registerCollection(String name, Class<?> elementType, Class<?> declaringType) {
		return register(name, true, elementType, declaringType, null);
	}

	public static DomainProperty registerCollection(String name, Class<?> elementType, Class<?> declaringType,
			BiFunction<DomainObject, DomainProperty, Object> getDefaultValue) {
		return register(name, true, elementType, declaringType, getDefaultValue);
	}

//
//	#
//	region 注册动态属性
//
//	/// <summary>
//	/// 注册类型为动态结构的领域属性
//	/// </summary>
//	/// <typeparam name="TD"></typeparam>
//	/// <typeparam name="OT"></typeparam>
//	/// <param name="name"></param>
//	/// <returns></returns>
//	public static DomainProperty RegisterDynamic<TD,OT>(
//	string name, Func<object,object,bool>compare)
//	where TD:
//	TypeDefine where OT:DomainObject
//	{
//		var ownerType=typeof(OT);var define=TypeDefine.GetDefine<TD>();
//
//		var propertyType=define.MetadataType;var result=Register(name,propertyType,ownerType,(o,p)=>{return define.EmptyInstance;},compare,define.MetadataType);define.SetBelongProperty(result);return result;
//	}
//
//	public static DomainProperty RegisterDynamic<TD,OT>(
//	string name)
//	where TD:
//	TypeDefine where OT:DomainObject
//	{
//		return RegisterDynamic<TD,OT>(name,null);
//	}
//
//	/// <summary>
//	/// 动态类型的集合
//	/// </summary>
//	/// <typeparam name="TD"></typeparam>
//	/// <typeparam name="OT"></typeparam>
//	/// <param name="propertyName"></param>
//	/// <returns></returns>
//	public static DomainProperty RegisterDynamicCollection<TD,OT>(
//	string propertyName, Func<object,object,bool>compare)
//	where TD:
//	TypeDefine where OT:DomainObject
//	{
//		var define=TypeDefine.GetDefine<TD>();var elementType=define.MetadataType; // 动态类型
//
//		return Register(propertyName,typeof(DomainCollection<dynamic>),typeof(OT),(owner,property)=>{var collection=new DomainCollection<dynamic>(property);collection.Parent=owner;return collection;},compare,elementType);
//	}
//
//	public static DomainProperty RegisterDynamicCollection<TD,OT>(
//	string propertyName)
//	where TD:
//	TypeDefine where OT:DomainObject
//	{
//		return RegisterDynamicCollection<TD,OT>(propertyName,null);
//	}
//
//	#endregion
//

//	region 辅助方法

	private static String getCall(Iterable<Annotation> anns) {
		var ann = getAnnotation(anns, PropertyLabel.class);
		return ann != null ? ann.name() : null;
	}

	/**
	 * 
	 * 获得属性与仓储有关的配置
	 * 
	 * @param propertyName
	 * @param declaringType
	 * @return
	 */
	private static PropertyRepositoryImpl getRepository(Iterable<Annotation> anns, Class<?> declaringType) {
		var ann = getAnnotation(anns, PropertyRepository.class);
		return ann != null ? new PropertyRepositoryImpl(ann, declaringType) : PropertyRepositoryImpl.Default;
	}

	@SuppressWarnings({ "unchecked", "unlikely-arg-type" })
	static <T extends Annotation> T getAnnotation(Iterable<Annotation> anns, Class<T> annType) {
		return (T) ListUtil.find(anns, (type) -> {
			return annType.equals(type);
		});
	}

	/**
	 * 获取领域属性定义的特性，标记在静态的领域属性字段上
	 * 
	 * @param objectType
	 * @param propertyName
	 * @return
	 */
	private static Iterable<Annotation> getAnnotations(Class<?> objectType, String propertyName) {
		ArrayList<Annotation> result = new ArrayList<>();

		// 在对象属性定义上查找特性
		ListUtil.addRange(result, getAnnotationsByStaticProperty(objectType, propertyName));

		// 从外部配置中得到，todo...
		// 获得objectMetaCode
		String objectMetaCode = null;
		if (objectMetaCode != null)
			DynamicProperty.register(objectType, objectMetaCode);

		return result;
	}

	/**
	 * 在静态的领域属性定义的字段上查找特性
	 */
	private static Annotation[] getAnnotationsByStaticProperty(Class<?> reflectedType, String propertyName) {
		try {
			Field field = reflectedType.getDeclaredField(String.format("%sProperty", propertyName));
			return field == null ? null : field.getAnnotations();
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	private static final MapList<Class<?>, DomainProperty> _propertis = new MapList<>(false);

	/**
	 * 
	 * 添加领域属性到数据集中，新版中我们要确保 {@code objectType} 一定等于 {@code property.declaringType()}
	 * 
	 * @param objectType
	 * @param property
	 */
	private static void addProperty(DomainProperty property) {
		_propertis.put(property.declaringType(), property);
	}

	public static Iterable<DomainProperty> getProperties(Class<?> objectType) {
		return _propertis.getValues(objectType);
	}

	public static DomainProperty getProperty(Class<?> objectType, String propertyName) {
		return _propertis.getValue(objectType, (p) -> {
			return p.name().equalsIgnoreCase(propertyName);
		});
	}

}
