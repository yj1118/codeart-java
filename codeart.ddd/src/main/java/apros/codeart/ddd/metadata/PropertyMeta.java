package apros.codeart.ddd.metadata;

import java.util.function.BiFunction;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.IEmptyable;
import apros.codeart.ddd.IPropertyDataLoader;
import apros.codeart.ddd.IPropertyValidator;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.runtime.TypeUtil;

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

	private DomainProperty _property;

	public DomainProperty getProperty() {
		if (_property == null) {
			_property = DomainProperty.getProperty(this.declaringType(), this.name());
		}
		return _property;
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

	public boolean isCollection() {
		return _value.isCollection();
	}

	private ObjectMeta _declaring;

	public ObjectMeta declaring() {
		return _declaring;
	}

	public Class<?> declaringType() {
		return _declaring.objectType();
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

	private Iterable<IPropertyValidator> _validators;

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

	private boolean _lazy;

	/**
	 * 属性是否为懒惰加载
	 * 
	 * @return
	 */
	public boolean lazy() {
		return _lazy;
	}

	private IPropertyDataLoader _dataLoader;

	/**
	 * 自定义加载器，大部分情况下不需要自定义加载器
	 * 
	 * @return
	 */
	public IPropertyDataLoader dataLoader() {
		return _dataLoader;
	}

	private String _fullName;

	public String fullName() {
		return _fullName;
	}

	private boolean _isEmptyable;

	public boolean isEmptyable() {
		return _isEmptyable;
	}

	public PropertyMeta(String name, ValueMeta value, ObjectMeta declaring, PropertyAccessLevel accessGet,
			PropertyAccessLevel accessSet, String call, Iterable<IPropertyValidator> validators, boolean lazy,
			IPropertyDataLoader dataLoader) {
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

		declaring.addProperty(this); // 关联

		_fullName = String.format("%s.%s", this.declaringType().getName(), this.name());

		_isEmptyable = IEmptyable.class.isAssignableFrom(this.monotype());
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
		var obj = ObjectMetaLoader.get(propertyName);
		return obj.findProperty(propertyName);
	}

	public static Iterable<PropertyMeta> getProperties(Class<?> doType) {
		var obj = ObjectMetaLoader.get(doType);
		return obj.properties();
	}
}
