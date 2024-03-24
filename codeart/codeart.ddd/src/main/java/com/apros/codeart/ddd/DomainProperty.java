package com.apros.codeart.ddd;

import static com.apros.codeart.runtime.Util.propagate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.apros.codeart.ddd.metadata.DomainPropertyCategory;
import com.apros.codeart.ddd.metadata.ObjectMeta;
import com.apros.codeart.ddd.metadata.PropertyAccessLevel;
import com.apros.codeart.ddd.metadata.PropertyMeta;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.FieldUtil;
import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.Guid;
import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.ListUtil;
import com.apros.codeart.util.MapList;
import com.apros.codeart.util.StringUtil;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

public class DomainProperty implements IDomainProperty {

	private PropertyMeta _meta;

	/**
	 * 属性名称
	 * 
	 * @return
	 */
	public String name() {
		return _meta.name();
	}

	/**
	 * 属性的类型，比如字符串/整型等
	 */
	private Class<?> _Type;

	public Class<?> Type() {
		return _meta.Type();
	}

//	private Class<?> _dynamicType;
//
//	/// <summary>
//	/// 类型定义，当属性是动态生成的时候该值提供类型定义,
//	/// 注意，当属性是基本类型是，DynamicType是真实类型
//	/// 当属性是定义的动态对象类型时，DynamicType是元数据类型，即：RuntimeObjectType
//	/// 当属性是定义的动态对象集合类型时，DynamicType是集合的成员元数据类型，即：RuntimeObjectType
//	/// 当属性是定义的基本类型集合类型时，DynamicType是集合的成员真实类型
//	/// </summary>
//	public Class<?> getDynamicType() {
//		return _dynamicType;
//	}

//	protected void setDynamicType(Class<?> value) {
//		_dynamicType = value;
//		_domainPropertyType = getDomainPropertyType(_propertyType, value);
//	}

	public boolean isDynamic() {
		return false;
//		return _meta.isDynamic();
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
	private Class<?> _declaringType;

	public Class<?> getDeclaringType() {
		return _declaringType;
	}

	void setDeclaringType(Class<?> value) {
		_declaringType = value;
	}

	private BiFunction<DomainObject, DomainProperty, Object> _getDefaultValue;

	/**
	 * 
	 * 获得属性的默认值,传入属性所在的对象和成员对应的属性定义
	 * 
	 * @param obj
	 * @param property
	 * @return
	 */
	public Object getDefaultValue(DomainObject obj, DomainProperty property) {
		return _getDefaultValue.apply(obj, property);
	}

	boolean isChanged(Object oldValue, Object newValue) {
		// 属性如果是集合、实体对象（引用对象），那么不用判断值，直接认为是被改变了
		if (TypeUtil.isCollection(this.propertyType()) || this.propertyType().isAssignableFrom(IEntityObject.class))
			return true;

		// 普通类型就用常规比较
		return !Objects.equal(oldValue, newValue);
	}

//	#endregion

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

	/**
	 * 属性验证器
	 */
	private Iterable<IPropertyValidator> _validators;

	public Iterable<IPropertyValidator> validators() {
		return _validators;
	}

//	#endregion
//
//	#region 属性仓储的定义

	PropertyRepositoryAnn _repositoryTip;

	PropertyRepositoryAnn getRepositoryTip() {
		return _repositoryTip;
	}

	private static MapList<Class<?>, DomainProperty> _properties = new MapList<Class<?>, DomainProperty>(false);

	/**
	 * 获取类型 {@code doType} 的所有属性定义
	 * 
	 * @param doType
	 * @return
	 */
	static Iterable<DomainProperty> getProperties(Class<?> doType) {
		// 使用_getProperties方法，而不是直接使用_properties对象
		// 是因为_properties记录的是对象类型Type上注册了哪些属性
		// 但是在继承关系上，比如A继承了B，B有10个领域属性，而A类型就无法通过_properties的记录获取领域属性
		// 因此需要用_getProperties方法
		return _getProperties.apply(doType);
	}

	/// <summary>
	/// 该方法可以获得一个类型包括继承链上所有的注册到的领域属性
	/// </summary>
	private static Function<Class<?>, Iterable<DomainProperty>> _getProperties = LazyIndexer.init((objectType) -> {
		// 由于领域属性都是在静态构造中定义，如果对象类型从来没有被使用过，那么就不会构造，就不会产生领域属性
		DomainObject.staticConstructor(objectType);

		ArrayList<DomainProperty> properties = new ArrayList<DomainProperty>();
		var types = TypeUtil.getInheriteds(objectType);

		for (var type : types) {
			Iterables.addAll(properties, _properties.getValues(type));
		}
		Iterables.addAll(properties, _properties.getValues(objectType));

		// 排序
		properties.sort((o1, o2) -> {
			// 让基类的属性在前
			return Integer.compare(TypeUtil.getDepth(o1.getDeclaringType()), TypeUtil.getDepth(o2.getDeclaringType()));
		});

		return properties;
	});

	static DomainProperty getProperty(Class<?> doType, String propertyName) {
		return _getPropertyByName.apply(doType).apply(propertyName);
	}

	private static Function<Class<?>, Function<String, DomainProperty>> _getPropertyByName = LazyIndexer
			.init((doType) -> {
				return LazyIndexer.<String, DomainProperty>init((propertyName) -> {
					var properies = getProperties(doType);
					var dp = Iterables.find(properies, (p) -> p.getName().equalsIgnoreCase(propertyName), null);
					if (dp == null)
						throw new DomainDrivenException(
								Language.strings("NotFoundDomainProperty", doType.getName(), propertyName));
					return dp;
				});
			});

	public DomainProperty(PropertyMeta meta) {
		_meta = meta;
	}

	private static class AccessInfo {
		public PropertyAccessLevel _accessGet;
		public PropertyAccessLevel _accessSet;

		public PropertyAccessLevel accessGet() {
			return _accessGet;
		}

		public PropertyAccessLevel accessSet() {
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

	private static String getCall(String propertyName, Class<?> declaringType) {
		var ann = getAnnotation(declaringType, propertyName, PropertyLabel.class);
		return ann != null ? ann.name() : null;
	}

	public static DomainProperty register(String name, Class<?> propertyType, Class<?> declaringType,
			BiFunction<DomainObject, DomainProperty, Object> getDefaultValue) {

		// todo
		/*
		 * if (this.isDynamic()) { // 动态属性的访问是公开的 _accessLevelGet =
		 * PropertyAccessLevel.Public; _accessLevelSet = PropertyAccessLevel.Public; }
		 * else { _accessLevelGet = FieldUtil.canRead(_field) ?
		 * PropertyAccessLevel.Public : PropertyAccessLevel.Private; _accessLevelSet =
		 * FieldUtil.canWrite(_field) ? PropertyAccessLevel.Public :
		 * PropertyAccessLevel.Private; }
		 */

		var access = getAccess(name, declaringType);
		var call = getCall(name, declaringType);

		var declaring = ObjectMeta.obtain(declaringType);

		var meta = new PropertyMeta(name, propertyType, declaring, access.accessGet(), access.accessSet(), call,
				getDefaultValue);

		return new DomainProperty(meta);
	}

//	#
//	region 直接设置默认值的注册属性的方法

	public static <PT,OT extends DomainObject> DomainProperty register(
	String name, BiFunction<Object, Object, Boolean> compare)
	{
	    return Register<PT, OT>(name, (o, p) ->
	    {
	        return DetectDefaultValue<PT>(); //此处不能在Register<PT, OT>(name, (o, p) => 之前执行DetectDefaultValue<PT>(),因为在领域对象的代码里，有可能注册属性放在空对象的定义之前，导致空对象是null，具体可见菜单示例
	    }, compare);
	}

//
//	public static DomainProperty Register<PT,OT>(string name)
//	where OT:DomainObject
//	{
//	    return Register<PT, OT>(name, (o, p) =>
//	    {
//	        return DetectDefaultValue<PT>(); //此处不能在Register<PT, OT>(name, (o, p) => 之前执行DetectDefaultValue<PT>(),因为在领域对象的代码里，有可能注册属性放在空对象的定义之前，导致空对象是null，具体可见菜单示例
//	    }, null);
//	}
//

//
//	/// <summary>
//	///
//	/// </summary>
//	/// <typeparam name="PT">属性的类型</typeparam>
//	/// <typeparam name="OT">所属领域对象的类型</typeparam>
//	/// <param name="name"></param>
//	/// <param name="defaultValue"></param>
//	/// <returns></returns>
//	public static DomainProperty Register<PT,OT>(
//	string name, PT defaultValue)
//	where OT:DomainObject
//	{
//	    return Register<PT, OT>(name, (owner) => defaultValue);
//	}
//
//	#endregion
//
//	#
//	region 注册集合
//
//	/// <summary>
//	/// 注册集合
//	/// </summary>
//	/// <typeparam name="MemberType"></typeparam>
//	/// <typeparam name="OT"></typeparam>
//	/// <param name="propertyName"></param>
//	/// <returns></returns>
//	public static DomainProperty RegisterCollection<MemberType,OT>(string propertyName)
//	where OT:DomainObject
//	{
//	    return Register(propertyName, typeof(DomainCollection<MemberType>), typeof(OT), (owner, property) =>
//	    {
//	        var collection = new DomainCollection<MemberType>(property);
//	        collection.Parent = owner;
//	        return collection;
//	    }, null);
//	}
//
//	public static DomainProperty RegisterCollection<MemberType,OT>(
//	string propertyName, Func<object,object,bool>compare)
//	where OT:DomainObject
//	{
//		return Register(propertyName,typeof(DomainCollection<MemberType>),typeof(OT),(owner,property)=>{var collection=new DomainCollection<MemberType>(property);collection.Parent=owner;return collection;},compare);
//	}
//
//	#endregion
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
//	/// <summary>
//	///
//	/// </summary>
//	/// <typeparam name="PT"></typeparam>
//	/// <typeparam name="OT"></typeparam>
//	/// <param name="name"></param>
//	/// <param name="getDefaultValue">由于引用对象作为默认值会被公用，互相受到影响，所以默认值以方法的形式提供</param>
//	/// <returns></returns>
//	public static DomainProperty Register<PT,OT>(
//	string name, Func<OT,PT>getDefaultValue)
//	where OT:DomainObject
//	{
//		return Register(name,typeof(PT),typeof(OT),(owner,property)=>{return getDefaultValue((OT)owner);},null);
//	}
//
//	public static DomainProperty Register<PT,OT>(
//	string name, Func<OT,PT>getDefaultValue,
//	Func<object, object, bool> compare)
//	where OT:DomainObject
//	{
//		return Register(name,typeof(PT),typeof(OT),(owner,property)=>{return getDefaultValue((OT)owner);},compare);
//	}
//
//	#endregion
//
//	#
//	region 辅助方法

	@SuppressWarnings("unchecked")
	<T extends Annotation> T getAnnotation(Class<T> annType) {
		return getAnnotation(_declaringType, _name, annType);
	}

	private static Function<Class<?>, Function<String, Iterable<Annotation>>> _getAnnotations = LazyIndexer
			.init((objectType) -> {
				return LazyIndexer.init((propertyName) -> {
					ArrayList<Annotation> result = new ArrayList<>();

					// 在对象属性定义上查找特性
					ListUtil.addRange(result, getAnnotationsByStaticProperty(objectType, propertyName));

					// 从外部配置中得到，todo...

					return result;
				});
			});

	/**
	 * 获取领域属性定义的特性，这些特性可以标记在对象属性上，也可以标记在静态的领域属性字段上
	 * 
	 * @param objectType
	 * @param propertyName
	 * @return
	 */
	static Iterable<Annotation> getAnnotations(Class<?> objectType, String propertyName) {
		return _getAnnotations.apply(objectType).apply(propertyName);
	}

	@SuppressWarnings({ "unchecked", "unlikely-arg-type" })
	static <T extends Annotation> T getAnnotation(Class<?> objectType, String propertyName, Class<T> annType) {
		return (T) ListUtil.find(getAnnotations(objectType, propertyName), (type) -> {
			return annType.equals(type);
		});
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
}
