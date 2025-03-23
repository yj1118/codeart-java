package apros.codeart.ddd.metadata;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

import apros.codeart.ddd.*;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.Guid;

public class PropertyMeta {

    private final UUID _id;

    public UUID id() {
        return _id;
    }

    private final String _name;

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

    /**
     * 单体类型，这个类型的意思是，当类型为集合时，是成员element的类型，当不是集合时，那就是单体自身的类型
     *
     * @return
     */
    public Class<?> monotype() {
        return _value.monotype();
    }

    /**
     * 属性值的类型
     */
    public Class<?> type() {
        return _value.getType();
    }

    /**
     * 单体类型的领域元数据描述，对于int/string等基元类型是没有该描述的
     *
     * @return
     */
    public ObjectMeta monoMeta() {
        return _value.monoMeta();
    }

    public boolean isCollection() {
        return _value.isCollection();
    }

    private final ObjectMeta _declaring;

    public ObjectMeta declaring() {
        return _declaring;
    }

    public Class<?> declaringType() {
        return _declaring.objectType();
    }

    private final DomainPropertyCategory _category;

    public DomainPropertyCategory category() {
        return _category;
    }

    private final PropertyAccessLevel _accessGet;

    public PropertyAccessLevel accessGet() {
        return _accessGet;
    }

    private final PropertyAccessLevel _accessSet;

    public PropertyAccessLevel accessSet() {
        return _accessSet;
    }

    public boolean isPublicSet() {
        return _accessSet == PropertyAccessLevel.Public;
    }

    private final String _call;

    /**
     * 称呼
     *
     * @return
     */
    public String call() {
        return _call;
    }

    public BiFunction<DomainObject, String, Object> getDefaultValue() {
        return _value.getDefaultValue();
    }

    private final Iterable<IPropertyValidator> _validators;

    public Iterable<IPropertyValidator> validators() {
        return _validators;
    }

    @SuppressWarnings("unchecked")
    public <T extends IPropertyValidator> T findValidator(Class<T> validatorType) {
        for (var validator : _validators) {
            if (validator.getClass().equals(validatorType))
                return (T) validator;
        }
        return null;
    }

    private final boolean _lazy;

    /**
     * 属性是否为懒惰加载
     *
     * @return
     */
    public boolean lazy() {
        return _lazy;
    }

    private final IPropertyDataLoader _dataLoader;

    /**
     * 自定义加载器，大部分情况下不需要自定义加载器
     *
     * @return
     */
    public IPropertyDataLoader dataLoader() {
        return _dataLoader;
    }

    private final String _fullName;

    public String fullName() {
        return _fullName;
    }

    private final boolean _isEmptyable;

    public boolean isEmptyable() {
        return _isEmptyable;
    }

    public PropertyMeta(String name, ValueMeta value, ObjectMeta declaring, PropertyAccessLevel accessGet,
                        PropertyAccessLevel accessSet, String call, Iterable<IPropertyValidator> validators, boolean lazy,
                        IPropertyDataLoader dataLoader) {
        _id = Guid.newGuid();
        _name = name;
        _value = value;
        _declaring = declaring;
        _category = getCategory(value);
        _accessGet = accessGet;
        _accessSet = accessSet;
        _call = call; // call可以为空，空表示没有使用多语言
        _validators = validators;

        _lazy = lazy;
        _dataLoader = dataLoader;

        _fullName = String.format("%s.%s", this.declaringType().getName(), this.name());

        _isEmptyable = IEmptyable.class.isAssignableFrom(this.monotype());
    }

    @Override
    public boolean equals(Object obj) {
        var target = TypeUtil.as(obj, PropertyMeta.class);
        if (target == null)
            return false;
        return Objects.equals(this.id(), target.id());
    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }

    @Override
    public String toString() {
        return "PropertyMeta{name='" + this.name() + "'}";
    }

    /**
     * 从注解中修正属性元数据信息，这主要用于子类修正父类定义的领域属性
     */
    void correct(ObjectProperty op) {
        if (op == null) return;
        _value = ValueMeta.createBy(this.value().isCollection(), op.type(), this.value().getDefaultValue());
    }

    private static DomainPropertyCategory getCategory(ValueMeta valueMeta) {

        if (valueMeta.isCollection()) {

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

    public static PropertyMeta getProperty(Class<?> doType, String propertyName) {
        var obj = ObjectMetaLoader.get(doType);
        return obj.findProperty(propertyName);
    }

    public static Iterable<PropertyMeta> getProperties(Class<?> doType) {
        var obj = ObjectMetaLoader.get(doType);
        return obj.properties();
    }
}
