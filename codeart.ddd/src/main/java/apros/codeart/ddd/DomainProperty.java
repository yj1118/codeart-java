package apros.codeart.ddd;

import static apros.codeart.runtime.Util.propagate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiFunction;

import com.google.common.base.Objects;

import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.metadata.PropertyAccessLevel;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.metadata.ValueMeta;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.PropertyRepository;
import apros.codeart.ddd.repository.PropertyRepositoryImpl;
import apros.codeart.runtime.FieldUtil;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;
import apros.codeart.util.MapList;
import apros.codeart.util.StringUtil;

/**
 * 注册属性元数据和使用元数据的职责
 * <p>
 * 但是不负责存放元数据
 */
public class DomainProperty {

    private final PropertyMeta _meta;

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
        return PropertyLabelImpl.getValue(_meta.call());
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
     * 获得属性的默认值,传入属性所在的对象和成员对应的属性定义
     *
     * @param obj
     * @param property
     * @return
     */
    public Object getDefaultValue(DomainObject obj, DomainProperty property) {
        return _meta.getDefaultValue().apply(obj, property.name());
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
        return _meta.isChanged(oldValue, newValue);
    }

    public UUID id() {
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

    private static AccessInfo getAccess(String propertyName, ValueMeta valueMeta, Class<?> declaringType) {

        var getter = FieldUtil.getFieldMethodGetter(propertyName, declaringType);
        var accessGet = getter == null ? PropertyAccessLevel.Private : PropertyAccessLevel.Public;

        var fieldType = valueMeta.getType();

        var setter = FieldUtil.getFieldMethodSetter(propertyName, fieldType, declaringType);
        var accessSet = setter == null ? PropertyAccessLevel.Private : PropertyAccessLevel.Public;

        return new AccessInfo(accessGet, accessSet);
    }

    private static DomainProperty register(String name, boolean isCollection, Class<?> monotype, Class<?> declaringType,
                                           BiFunction<DomainObject, String, Object> getDefaultValue) {

        var valueMeta = ValueMeta.createBy(isCollection, monotype, getDefaultValue);
        // 获得属性上所有的特性标签
        var anns = getAnnotations(declaringType, name);

        var access = getAccess(name, valueMeta, declaringType);
        var call = getCall(anns);

        var declaring = ObjectMetaLoader.get(declaringType);

        var validators = PropertyValidatorImpl.getValidators(anns);

        var repositoryTip = getRepository(anns, declaringType);

        var meta = new PropertyMeta(name, valueMeta, declaring, access.get(), access.set(), call, validators,
                repositoryTip.lazy(), repositoryTip.loader());

        declaring.addProperty(meta); // 关联

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

    public static DomainProperty register(String name, Class<?> propertyType, Class<?> declaringType,
                                          BiFunction<DomainObject, String, Object> getDefaultValue) {
        return register(name, false, propertyType, declaringType, getDefaultValue);
    }

    /**
     * 通过领域属性的类型名称来注册领域属性，该方法非常适用于动态领域对象
     *
     * @param name
     * @param propertyTypeName
     * @param declaringType
     * @return
     */
    public static DomainProperty register(String name, String propertyTypeName, Class<?> declaringType) {

        var propertyType = ObjectMetaLoader.get(propertyTypeName).objectType();
        return register(name, false, propertyType, declaringType, null);
    }

    /**
     * 通过领域属性的类型名称来注册集合
     *
     * @param name
     * @param elementTypeName
     * @param declaringType
     * @return
     */
    public static DomainProperty registerCollection(String name, String elementTypeName, Class<?> declaringType) {
        var elementType = ObjectMetaLoader.get(elementTypeName).objectType();
        return register(name, true, elementType, declaringType, null);
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
                                                    BiFunction<DomainObject, String, Object> getDefaultValue) {
        return register(name, true, elementType, declaringType, getDefaultValue);
    }


    //region 包装方法，主要是为了给动态生成类用，程序员也可以调用

    public static DomainProperty registerByte(String name, Class<?> declaringType) {
        return register(name, byte.class, declaringType);
    }

    public static DomainProperty registerShort(String name, Class<?> declaringType) {
        return register(name, short.class, declaringType);
    }

    public static DomainProperty registerInt(String name, Class<?> declaringType) {
        return register(name, int.class, declaringType);
    }

    public static DomainProperty registerLong(String name, Class<?> declaringType) {
        return register(name, long.class, declaringType);
    }

    public static DomainProperty registerFloat(String name, Class<?> declaringType) {
        return register(name, float.class, declaringType);
    }

    public static DomainProperty registerDouble(String name, Class<?> declaringType) {
        return register(name, double.class, declaringType);
    }

    public static DomainProperty registerBoolean(String name, Class<?> declaringType) {
        return register(name, boolean.class, declaringType);
    }

    public static DomainProperty registerString(String name, Class<?> declaringType) {
        return register(name, String.class, declaringType);
    }

    public static DomainProperty registerChar(String name, Class<?> declaringType) {
        return register(name, char.class, declaringType);
    }

    //endregion

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
        return ann != null ? ann.value() : null;
    }

    /**
     * 获得属性与仓储有关的配置
     *
     * @param declaringType
     * @return
     */
    private static PropertyRepositoryImpl getRepository(Iterable<Annotation> anns, Class<?> declaringType) {
        var ann = getAnnotation(anns, PropertyRepository.class);
        return ann != null ? new PropertyRepositoryImpl(ann, declaringType) : PropertyRepositoryImpl.Default;
    }

    @SuppressWarnings({"unchecked", "unlikely-arg-type"})
    static <T extends Annotation> T getAnnotation(Iterable<Annotation> anns, Class<T> annType) {
        return (T) ListUtil.find(anns, (ann) -> {
            return annType == ann.annotationType();
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
        // 自定义注解在动态创建类或者动态改写现有类时已经实现了对象属性的重写，不需要再额外处理
        ListUtil.addRange(result, getAnnotationsByStaticProperty(objectType, propertyName));

        return result;
    }

    /**
     * 在静态的领域属性定义的字段上查找特性
     */
    private static Annotation[] getAnnotationsByStaticProperty(Class<?> reflectedType, String propertyName) {
        try {
            Field field = reflectedType.getDeclaredField(String.format("%sProperty", propertyName));
            return field == null ? null : field.getAnnotations();
        } catch (Throwable e) {
            throw propagate(e);
        }
    }

    private static final MapList<Class<?>, DomainProperty> _properties = new MapList<>(false);

    /**
     * 添加领域属性到数据集中，新版中我们要确保 {@code objectType} 一定等于 {@code property.declaringType()}
     *
     * @param property
     */
    private static void addProperty(DomainProperty property) {
        _properties.put(property.declaringType(), property);
    }

//    /**
//     * 根据属性元数据，添加领域属性到定义中，这是因为继承关系，需要补充
//     *
//     * @param meta
//     */
//    public static void addProperty(PropertyMeta meta) {
//        var property = new DomainProperty(meta);
//        addProperty(property);
//    }
//
//    public static Iterable<DomainProperty> getProperties(Class<?> objectType) {
//        return _properties.getValues(objectType);
//    }

//    public static DomainProperty getProperty(Class<?> objectType, String propertyName) {
//        return _properties.getValue(objectType, (p) -> {
//            return p.name().equalsIgnoreCase(propertyName);
//        });
//    }
//
//    public static DomainProperty getProperty(PropertyMeta tip) {
//        return getProperty(tip.declaringType(), tip.name());
//    }

}
