package com.apros.codeart.ddd;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

import com.apros.codeart.runtime.TypeUtil;
import com.apros.codeart.util.MapList;
import com.google.common.base.Objects;

public class DomainProperty implements IDomainProperty {

	private String _name;

	/**
	 * 属性名称
	 * 
	 * @return
	 */
	public String getName() {
		return _name;
	}

	protected void setName(String value) {
		_name = value;
	}

	private Class<?> _propertyType;

	public Class<?> getPropertyType() {
		return _propertyType;
	}

	protected void setPropertyType(Class<?> value) {
		_propertyType = value;
		_domainPropertyType = getDomainPropertyType(value, _dynamicType);
	}

	private Field _field;

	Field getFieldInfo() {
		return _field;
	}

//	/// <summary>
//	/// 指示属性是否为扩展的(java 里没有扩展的机制)
//	/// </summary>
//	public bool IsExtensions
//	{
//	    get
//	    {
//	        return this.PropertyInfo == null;
//	    }
//	}

	private Class<?> _dynamicType;

	/// <summary>
	/// 类型定义，当属性是动态生成的时候该值提供类型定义,
	/// 注意，当属性是基本类型是，DynamicType是真实类型
	/// 当属性是定义的动态对象类型时，DynamicType是元数据类型，即：RuntimeObjectType
	/// 当属性是定义的动态对象集合类型时，DynamicType是集合的成员元数据类型，即：RuntimeObjectType
	/// 当属性是定义的基本类型集合类型时，DynamicType是集合的成员真实类型
	/// </summary>
	public Class<?> getDynamicType() {
		return _dynamicType;
	}

	protected void setDynamicType(Class<?> value) {
		_dynamicType = value;
		_domainPropertyType = getDomainPropertyType(_propertyType, value);
	}

	public boolean isDynamic() {
		return _dynamicType != null;
	}

	private PropertyAccessLevel _accessLevelSet;

	public PropertyAccessLevel accessLevelSet() {
		return _accessLevelSet;
	}

	private PropertyAccessLevel _accessLevelGet;

	public PropertyAccessLevel accessLevelGet() {
		return _accessLevelGet;
	}

	private PropertyLabelAnnotation _label;

	public PropertyLabelAnnotation getLabel() {
		return _label;
	}

	/**
	 * 称呼（支持多语言）
	 * 
	 * @return
	 */
	public String call() {
		return _label == null ? this.getName() : _label.getValue();
	}

	private DomainPropertyType _domainPropertyType;

	DomainPropertyType getDomainPropertyType() {
		return _domainPropertyType;
	}

	private void setDomainPropertyType(DomainPropertyType value) {
		_domainPropertyType = value;
	}

	/**
	 * 属性是否引用的是内聚根（或内聚根集合）
	 * 
	 * @return
	 */
	boolean isQuoteAggreateRoot() {
		return _domainPropertyType == DomainPropertyType.AggregateRoot
				|| _domainPropertyType == DomainPropertyType.AggregateRootList;
	}

	static DomainPropertyType getDomainPropertyType(Class<?> propertyType, Class<?> dynamicType) {

		if (TypeUtil.isCollection(propertyType)) {

			var elementType = dynamicType != null ? dynamicType : TypeUtil.resolveElementType(propertyType);

			if (DomainObject.isAggregateRoot(elementType))
				return DomainPropertyType.AggregateRootList;
			if (DomainObject.isEntityObject(elementType))
				return DomainPropertyType.EntityObjectList;
			if (DomainObject.isValueObject(elementType))
				return DomainPropertyType.ValueObjectList;
			return DomainPropertyType.PrimitiveList;
		} else {
//			var targetType = dynamicType != null ? dynamicType : propertyType;  .net版本代码有这个变量，但是.net里也没用上

			if (DomainObject.isAggregateRoot(propertyType))
				return DomainPropertyType.AggregateRoot;
			if (DomainObject.isEntityObject(propertyType))
				return DomainPropertyType.EntityObject;
			if (DomainObject.isValueObject(propertyType))
				return DomainPropertyType.ValueObject;
			return DomainPropertyType.Primitive;
		}

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

	private BiFunction<Object, Object, Boolean> _compare;

	public void setCompare(BiFunction<Object, Object, Boolean> value) {
		_compare = value;
	}

	private Boolean compare(Object obj0, Object obj1) {
		return _compare.apply(obj0, obj1);
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

//	/// <summary>
//	/// 设置属性值的行为链
//	/// </summary>
//	internal PropertyGetChain GetChain
//	{
//	    get;
//	    private set;
//	}
//
//	/// <summary>
//	/// 设置属性值的行为链
//	/// </summary>
//	internal PropertySetChain SetChain
//	{
//	    get;
//	    private set;
//	}
//
//	/// <summary>
//	/// 更改属性值的行为链
//	/// </summary>
//	internal PropertyChangedChain ChangedChain
//	{
//	    get;
//	    private set;
//	}
//
//	public bool IsRegisteredChanged
//	{
//	    get
//	    {
//	        return this.ChangedChain.MethodsCount > 0; 
//	    }
//	}

//	#region 比较

	boolean isChanged(Object oldValue, Object newValue) {
		if (this._compare == null) {
			// 默认比较
			// 属性如果是集合、实体对象（引用对象），那么不用判断值，直接认为是被改变了

			if (TypeUtil.isCollection(_propertyType) || _propertyType.isAssignableFrom(IEntityObject.class))
				return true;

			// 普通类型就用常规比较
			return !Objects.equal(oldValue, newValue);
		}
		return !this.compare(oldValue, newValue);
	}

//	#endregion

	private String _id;

	public String getId() {
		return _id;
	}

	@Override
	public boolean equals(Object obj) {
		var target = TypeUtil.as(obj, DomainProperty.class);
		if (target == null)
			return false;
		return this.getId() == target.getId();
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}

	/**
	 * 属性验证器
	 */
	private Iterable<IPropertyValidator> _validators;

	public Iterable<IPropertyValidator> getValidators() {
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
		return _getProperties(doType);
	}

	/// <summary>
	/// 该方法可以获得一个类型包括继承链上所有的注册到的领域属性
	/// </summary>
	private static Function<Type, IEnumerable<DomainProperty>> _getProperties = LazyIndexer.Init<Type, IEnumerable<DomainProperty>>((objectType)=>
	{
	    //由于领域属性都是在静态构造中定义，如果对象类型从来没有被使用过，那么就不会构造，就不会产生领域属性
	    DomainObject.StaticConstructor(objectType);

	    List<DomainProperty> properties = new List<DomainProperty>();
	    var types = objectType.GetInheriteds();
	    foreach (var type in types)
	    {
	        properties.AddRange(_properties.GetValues(type));
	    }
	    properties.AddRange(_properties.GetValues(objectType));

	    //排序
	    return properties.OrderBy((p) =>
	    {
	        //让基类的属性在前
	        return p.OwnerType.GetDepth();
	    });
	});

	internal

	static DomainProperty GetProperty(Type doType, string propertyName)
	{
	    return _getPropertyByName(doType)(propertyName);
	}

	private static Func<Type, Func<string, DomainProperty>> _getPropertyByName = LazyIndexer.Init<Type, Func<string,DomainProperty>>((doType)=>
	{
	    return LazyIndexer.Init<string, DomainProperty>((propertyName) =>
	    {
	        var properies = GetProperties(doType);
	        var dp = properies.FirstOrDefault((p) => p.Name.EqualsIgnoreCase(propertyName));
	        if (dp == null)
	            throw new DomainDrivenException("在类型" + doType.FullName + "和其继承链上没有找到领域属性" + propertyName + "的定义");
	        return dp;
	    });
	});

	#region 完整构造

	public static DomainProperty Register(string name, 
	                                    Type propertyType,
	                                    Type ownerType, 
	                                    Func<DomainObject, DomainProperty, object> getDefaultValue,
	                                    Func<object,object,bool> compare,
	                                    Type dynamicType = null)
	{
	    lock (_properties)
	    {
	        var target = _properties.GetValue(ownerType, (p) =>
	        {
	            return p.Name.EqualsIgnoreCase(name);
	        });
	        if (target != null)
	            throw new DomainDrivenException(string.Format(Strings.RepeatedDeclareProperty, ownerType.FullName, name));

	        var validators = PropertyValidatorAttribute.GetValidators(ownerType, name);
	        var repositoryTip = GetAttribute<PropertyRepositoryAttribute>(ownerType, name);
	        var logableTip = GetAttribute<PropertyLogableAttribute>(ownerType, name);
	        var labelTip = GetAttribute<PropertyLabelAttribute>(ownerType, name);

	        var property = new DomainProperty()
	        {
	            Id = Guid.NewGuid(),
	            Name = name,
	            PropertyType = propertyType,
	            OwnerType = ownerType,
	            GetDefaultValue = getDefaultValue,
	            Compare = compare,
	            Validators = validators,
	            RepositoryTip = repositoryTip,
	            LogableTip = logableTip,
	            PropertyInfo = ownerType.ResolveProperty(name),
	            DynamicType = dynamicType,
	            Label = labelTip
	        };

	        if(repositoryTip != null)
	            repositoryTip.Property = property; //赋值

	        {
	            //获取属性值的行为链
	            var chain = new PropertyGetChain(property);
	            chain.AddMethods(PropertyGetAttribute.GetMethods(ownerType, name));
	            property.GetChain = chain;
	        }

	        {
	            //设置属性值的行为链
	            var chain = new PropertySetChain(property);
	            chain.AddMethods(PropertySetAttribute.GetMethods(ownerType, name));
	            property.SetChain = chain;
	        }


	        {
	            //更改属性值的行为链
	            var chain = new PropertyChangedChain(property);
	            chain.AddMethods(PropertyChangedAttribute.GetMethods(ownerType, name));
	            property.ChangedChain = chain;
	        }

	        InitAccessLevel(property);

	        _properties.Add(ownerType, property);
	        return property;
	    }
	}

	private static void InitAccessLevel(DomainProperty property) {
		if (property.IsExtensions) {
			var method = ExtendedClassAttribute.GetPropertyMethod(property.OwnerType, property.Name);

			property.AccessLevelGet = GetAccessLevel(method.Get);
			property.AccessLevelSet = GetAccessLevel(method.Set);
		} else {
			var pi = property.PropertyInfo;

			if (property.IsDynamic) {
				// 动态属性的访问是公开的
				property.AccessLevelGet = PropertyAccessLevel.Public;
				property.AccessLevelSet = PropertyAccessLevel.Public;
			} else {
				property.AccessLevelGet = GetAccessLevel(pi.GetMethod);
				property.AccessLevelSet = GetAccessLevel(pi.SetMethod);
			}
		}
	}

	private static PropertyAccessLevel GetAccessLevel(MethodInfo method) {
		if (method == null || method.IsPrivate)
			return PropertyAccessLevel.Private;
		if (method.IsPublic)
			return PropertyAccessLevel.Public;
		return PropertyAccessLevel.Protected;
	}

	public static DomainProperty Register<PT,OT>(
	string name, Func<DomainObject,DomainProperty,object>getDefaultValue,
	Func<object, object, bool> compare)
	{
		return Register(name, typeof(PT), typeof(OT), getDefaultValue, compare);
	}

	#endregion

	#
	region 直接设置默认值的注册属性的方法

	public static DomainProperty Register<PT,OT>(
	string name, Func<object,object,bool>compare)
	where OT:DomainObject
	{
	    return Register<PT, OT>(name, (o, p) =>
	    {
	        return DetectDefaultValue<PT>(); //此处不能在Register<PT, OT>(name, (o, p) => 之前执行DetectDefaultValue<PT>(),因为在领域对象的代码里，有可能注册属性放在空对象的定义之前，导致空对象是null，具体可见菜单示例
	    }, compare);
	}

	public static DomainProperty Register<PT,OT>(string name)
	where OT:DomainObject
	{
	    return Register<PT, OT>(name, (o, p) =>
	    {
	        return DetectDefaultValue<PT>(); //此处不能在Register<PT, OT>(name, (o, p) => 之前执行DetectDefaultValue<PT>(),因为在领域对象的代码里，有可能注册属性放在空对象的定义之前，导致空对象是null，具体可见菜单示例
	    }, null);
	}

	private static object DetectDefaultValue<PT>()
	{
		var type = typeof(PT);
		return _detectDefaultValue(type);
	}

	private static Func<Type, object> _detectDefaultValue = LazyIndexer.Init<Type, object>((type)=>
	{
		if (type == typeof(string))
			return string.Empty;
		if (DomainObject.IsDomainObject(type)) {
			return DomainObject.GetEmpty(type);
		}
		return DataUtil.GetDefaultValue(type);
	});

	/// <summary>
	///
	/// </summary>
	/// <typeparam name="PT">属性的类型</typeparam>
	/// <typeparam name="OT">所属领域对象的类型</typeparam>
	/// <param name="name"></param>
	/// <param name="defaultValue"></param>
	/// <returns></returns>
	public static DomainProperty Register<PT,OT>(
	string name, PT defaultValue)
	where OT:DomainObject
	{
	    return Register<PT, OT>(name, (owner) => defaultValue);
	}

	#endregion

	#
	region 注册集合

	/// <summary>
	/// 注册集合
	/// </summary>
	/// <typeparam name="MemberType"></typeparam>
	/// <typeparam name="OT"></typeparam>
	/// <param name="propertyName"></param>
	/// <returns></returns>
	public static DomainProperty RegisterCollection<MemberType,OT>(string propertyName)
	where OT:DomainObject
	{
	    return Register(propertyName, typeof(DomainCollection<MemberType>), typeof(OT), (owner, property) =>
	    {
	        var collection = new DomainCollection<MemberType>(property);
	        collection.Parent = owner;
	        return collection;
	    }, null);
	}

	public static DomainProperty RegisterCollection<MemberType,OT>(
	string propertyName, Func<object,object,bool>compare)
	where OT:DomainObject
	{
		return Register(propertyName,typeof(DomainCollection<MemberType>),typeof(OT),(owner,property)=>{var collection=new DomainCollection<MemberType>(property);collection.Parent=owner;return collection;},compare);
	}

	#endregion

	#
	region 注册动态属性

	/// <summary>
	/// 注册类型为动态结构的领域属性
	/// </summary>
	/// <typeparam name="TD"></typeparam>
	/// <typeparam name="OT"></typeparam>
	/// <param name="name"></param>
	/// <returns></returns>
	public static DomainProperty RegisterDynamic<TD,OT>(
	string name, Func<object,object,bool>compare)
	where TD:
	TypeDefine where OT:DomainObject
	{
		var ownerType=typeof(OT);var define=TypeDefine.GetDefine<TD>();

		var propertyType=define.MetadataType;var result=Register(name,propertyType,ownerType,(o,p)=>{return define.EmptyInstance;},compare,define.MetadataType);define.SetBelongProperty(result);return result;
	}

	public static DomainProperty RegisterDynamic<TD,OT>(
	string name)
	where TD:
	TypeDefine where OT:DomainObject
	{
		return RegisterDynamic<TD,OT>(name,null);
	}

	/// <summary>
	/// 动态类型的集合
	/// </summary>
	/// <typeparam name="TD"></typeparam>
	/// <typeparam name="OT"></typeparam>
	/// <param name="propertyName"></param>
	/// <returns></returns>
	public static DomainProperty RegisterDynamicCollection<TD,OT>(
	string propertyName, Func<object,object,bool>compare)
	where TD:
	TypeDefine where OT:DomainObject
	{
		var define=TypeDefine.GetDefine<TD>();var elementType=define.MetadataType; // 动态类型

		return Register(propertyName,typeof(DomainCollection<dynamic>),typeof(OT),(owner,property)=>{var collection=new DomainCollection<dynamic>(property);collection.Parent=owner;return collection;},compare,elementType);
	}

	public static DomainProperty RegisterDynamicCollection<TD,OT>(
	string propertyName)
	where TD:
	TypeDefine where OT:DomainObject
	{
		return RegisterDynamicCollection<TD,OT>(propertyName,null);
	}

	#endregion

	/// <summary>
	///
	/// </summary>
	/// <typeparam name="PT"></typeparam>
	/// <typeparam name="OT"></typeparam>
	/// <param name="name"></param>
	/// <param name="getDefaultValue">由于引用对象作为默认值会被公用，互相受到影响，所以默认值以方法的形式提供</param>
	/// <returns></returns>
	public static DomainProperty Register<PT,OT>(
	string name, Func<OT,PT>getDefaultValue)
	where OT:DomainObject
	{
		return Register(name,typeof(PT),typeof(OT),(owner,property)=>{return getDefaultValue((OT)owner);},null);
	}

	public static DomainProperty Register<PT,OT>(
	string name, Func<OT,PT>getDefaultValue,
	Func<object, object, bool> compare)
	where OT:DomainObject
	{
		return Register(name,typeof(PT),typeof(OT),(owner,property)=>{return getDefaultValue((OT)owner);},compare);
	}

	#endregion

	#
	region 辅助方法

	internal T GetAttribute<T>()
	where T:Attribute
	{
		return GetAttribute<T>(this.OwnerType,this.Name);
	}

	/// <summary>
	/// 获取领域属性定义的特性，这些特性可以标记在对象属性上，也可以标记在静态的领域属性字段上
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="objectType"></param>
	/// <param name="propertyName"></param>
	/// <returns></returns>
	internal
	static T GetAttribute<T>(
	Type objectType, string propertyName)
	where T:Attribute
	{
		return GetAttributes<T>(objectType,propertyName).LastOrDefault(); // 用最后一个替代之前定义的
	}

	/// <summary>
	/// 获取领域属性定义的特性，这些特性可以标记在对象属性上，也可以标记在静态的领域属性字段上
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="objectType"></param>
	/// <param name="propertyName"></param>
	/// <returns></returns>
	internal
	static IEnumerable<T> GetAttributes<T>(
	Type objectType, string propertyName)
	where T:Attribute
	{
		List<T>result=new List<T>();

		// 在对象属性定义上查找特性
		result.AddRange(GetAttributesFromObjectProperty<T>(objectType,propertyName));

		// 从对象内部定义的领域属性上查找
		result.AddRange(GetAttributesFromDomainProperty<T>(objectType,propertyName));

		// 从扩展对象的领域属性上查找
		var extensionTypes=ExtendedClassAttribute.GetExtensionTypes(objectType);foreach(var extensionType in extensionTypes){result.AddRange(GetAttributesFromDomainProperty<T>(extensionType,propertyName));}

		return result;
	}

	/// <summary>
	/// 在对象属性定义上查找特性
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="objectType"></param>
	/// <param name="propertyName"></param>
	/// <returns></returns>
	private static IEnumerable<T> GetAttributesFromObjectProperty<T>(
	Type objectType, string propertyName)
	where T:Attribute
	{
		// 在属性定义上查找特性
		var propertyInfo=objectType.ResolveProperty(propertyName);if(propertyInfo!=null){var attributes=Attribute.GetCustomAttributes(propertyInfo,true).OfType<T>();// 会在继承链中查找
		foreach(var attr in attributes){var paa=attr as PropertyActionAttribute;if(paa!=null)paa.ObjectOrExtensionType=objectType;}return attributes;}return Array.Empty<T>();
	}

	/// <summary>
	/// 在静态的领域属性定义的字段上查找特性
	/// </summary>
	/// <typeparam name="T"></typeparam>
	/// <param name="objectType"></param>
	/// <param name="propertyName"></param>
	/// <returns></returns>
	private static IEnumerable<T> GetAttributesFromDomainProperty<T>(
	Type reflectedType, string propertyName)
	where T:Attribute
	{
		var fieldInfo=reflectedType.ResolveField(string.Format("{0}Property",propertyName));if(fieldInfo!=null){var attributes=Attribute.GetCustomAttributes(fieldInfo,true).OfType<T>();// 会在继承链中查找
		foreach(var attr in attributes){var paa=attr as PropertyActionAttribute;if(paa!=null)paa.ObjectOrExtensionType=reflectedType;}return attributes;}return Array.Empty<T>();
	}

	/// <summary>
	/// 指示属性是用于扩展已有属性的
	/// </summary>

	public static readonly DomainProperty Exists=new DomainProperty();

	#endregion

	#
	region 运行时编号

	internal readonly
	Guid RuntimeGetId = Guid.NewGuid();

	internal readonly
	Guid RuntimeSetId = Guid.NewGuid();

	internal readonly
	Guid RuntimeChangedId = Guid.NewGuid();

}
