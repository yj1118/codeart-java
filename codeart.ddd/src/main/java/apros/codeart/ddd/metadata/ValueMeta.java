package apros.codeart.ddd.metadata;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import apros.codeart.ddd.DomainCollection;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.Emptyable;
import apros.codeart.ddd.IEmptyable;
import apros.codeart.util.Guid;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

public class ValueMeta {

	private boolean _isCollection;

	public boolean isCollection() {
		return _isCollection;
	}

	private Class<?> _monotype;

	/**
	 * 单体类型，这个类型的意思是，当类型为集合时，是成员element的类型，当不是集合时，那就是单体自身的类型
	 * 
	 * @return
	 */
	public Class<?> monotype() {
		return _monotype;
	}

	private ObjectMeta _monoMeta;

	/**
	 * 
	 * 单体类型的领域元数据描述，对于int/string等基元类型是没有该描述的
	 * 
	 * @return
	 */
	public ObjectMeta monoMeta() {
		return _monoMeta;
	}

	private BiFunction<DomainObject, DomainProperty, Object> _getDefaultValue;

	public BiFunction<DomainObject, DomainProperty, Object> getDefaultValue() {
		return _getDefaultValue;
	}

	private ValueMeta(boolean isCollection, Class<?> monotype, ObjectMeta monoMeta,
			BiFunction<DomainObject, DomainProperty, Object> getDefaultValue) {
		_isCollection = isCollection;
		_monotype = monotype;
		_monoMeta = monoMeta;
		_getDefaultValue = getDefaultValue == null ? (obj, pro) -> {
			if (_isCollection) {
				var collection = new DomainCollection<>(monotype, pro);
				collection.setParent(obj);
				return collection;
			}
			return _detectDefaultValue.apply(this);
		} : getDefaultValue;
	}

	private static Function<ValueMeta, Object> _detectDefaultValue = LazyIndexer.init((valueMeta) -> {

		var valueType = valueMeta.monotype();

		if (ObjectMeta.isDomainObject(valueType)) {
			return DomainObject.getEmpty(valueType);
		}
		if (valueType.equals(String.class))
			return StringUtil.empty();

		if (valueType.equals(UUID.class))
			return Guid.Empty;

		if (valueType.equals(LocalDate.class))
			return LocalDate.MIN;

		if (valueType.equals(int.class) || valueType.equals(Integer.class) || valueType.equals(long.class)
				|| valueType.equals(byte.class) || valueType.equals(Byte.class) || valueType.equals(Long.class)
				|| valueType.equals(float.class) || valueType.equals(Float.class) || valueType.equals(double.class)
				|| valueType.equals(Double.class) || valueType.equals(short.class) || valueType.equals(Short.class))
			return 0;

		if (valueType.isAssignableFrom(IEmptyable.class))
			return Emptyable.createEmpty(valueType);

		if (valueType.equals(char.class))
			return StringUtil.empty();

		return null;

	});

	/**
	 * 
	 * 根据值的类型，创建值的元数据
	 * 
	 * @param valueType
	 * @return
	 */
	public static ValueMeta createBy(boolean isCollection, Class<?> monotype,
			BiFunction<DomainObject, DomainProperty, Object> getDefaultValue) {
		return new ValueMeta(isCollection, monotype, ObjectMetaLoader.tryGet(monotype), getDefaultValue);
	}

	/**
	 * 创建非集合类型得值的元数据
	 * 
	 * @param monotype
	 * @return
	 */
	public static ValueMeta createBy(Class<?> monotype) {
		return createBy(false, monotype, null);
	}

}
