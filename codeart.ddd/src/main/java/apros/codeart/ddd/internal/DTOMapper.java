package apros.codeart.ddd.internal;

import static apros.codeart.runtime.Util.propagate;

import java.util.function.Function;

import com.google.common.collect.Iterables;

import apros.codeart.ddd.ConstructorRepositoryImpl;
import apros.codeart.ddd.DomainCollection;
import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.dynamic.IDynamicObject;
import apros.codeart.ddd.metadata.DomainPropertyCategory;
import apros.codeart.ddd.metadata.ObjectMetaLoader;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.runtime.Activator;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;
import apros.codeart.util.PrimitiveUtil;

public final class DTOMapper {
	private DTOMapper() {
	}

	public static DTObject toDTO(DomainObject target, Function<DomainObject, Iterable<String>> getPropertes) {
		return toDTO(target, getPropertes, (p) -> p);
	}

	@SuppressWarnings("unchecked")
	public static <T> DTObject toDTO(DomainObject target, Function<DomainObject, Iterable<T>> getPropertyNames,
			Function<T, String> getPropertyName) {
		var properties = getPropertyNames.apply(target);

		var data = DTObject.editable();
		for (var property : properties) {
			var propertyName = getPropertyName.apply(property);
			var value = target.getValue(propertyName);
			var obj = TypeUtil.as(value, DomainObject.class);
			if (obj != null) {
				value = toDTO(obj, getPropertyNames, getPropertyName); // 对象
				data.setValue(propertyName, value);
				continue;
			}

			var list = TypeUtil.as(value, Iterable.class);
			if (list != null) {
				// 集合
				data.push(propertyName, list, (item) -> {
					var o = TypeUtil.as(item, DomainObject.class);
					if (o != null)
						return toDTO(o, getPropertyNames, getPropertyName); // 对象

					return DTObject.value(item);
				});
				continue;
			}

			data.setValue(propertyName, value); // 值
		}
		return data;
	}

	/**
	 * 从dto中加载数据
	 */
	@SuppressWarnings("unchecked")
	public static void load(DomainObject target, DTObject data) {
		var meta = ObjectMetaLoader.get(target.getClass());

		for (var property : meta.properties()) {
			var value = data.getValue(property.name(), false);
			if (value == null)
				continue;

			var obj = TypeUtil.as(value, DTObject.class);
			if (obj != null) {
				target.loadValue(property.name(), getObjectValue(target, property, obj));
				continue;
			}

			var objs = TypeUtil.as(value, Iterable.class);
			if (objs != null) {
				target.loadValue(property.name(), getListValue(target, property, objs));
				continue;
			}
			target.loadValue(property.name(), getPrimitiveValue(target, property, value));
		}
	}

	private static Object getObjectValue(DomainObject parent, PropertyMeta property, DTObject value) {
		switch (property.category()) {
		case DomainPropertyCategory.AggregateRoot:
		case DomainPropertyCategory.EntityObject:
		case DomainPropertyCategory.ValueObject: {

			var objType = property.monotype();
			return createInstance(objType, value);
		}
		default:
			throw new DomainDrivenException(Language.strings("DomainObjectLoadError", parent.getClass().getName()));
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object getListValue(DomainObject parent, PropertyMeta property, Iterable<DTObject> values) {
		var elementType = property.monotype();
		DomainProperty propertyInParent = property.getProperty();

		// 按照道理说，运行时是可以将DomainCollection转换为 DomainCollection<E>的，因为类型擦除了，但是还是测试下比较好
		var list = new DomainCollection(elementType, propertyInParent);
		list.setParent(parent);

		switch (property.category()) {
		case DomainPropertyCategory.AggregateRootList:
		case DomainPropertyCategory.EntityObjectList:
		case DomainPropertyCategory.ValueObjectList: {
			for (DTObject value : values) {
				var obj = createInstance(elementType, value);
				list.add(obj);
			}
			return list;
		}
		case DomainPropertyCategory.PrimitiveList: {
			for (DTObject value : values) {
				if (!value.isSingleValue())
					throw new DomainDrivenException(
							Language.strings("DomainObjectLoadError", parent.getClass().getName()));
				list.add(value.getValue());
			}
			return list;
		}
		default:
			throw new DomainDrivenException(Language.strings("DomainObjectLoadError", parent.getClass().getName()));
		}

	}

	private static Object getPrimitiveValue(DomainObject parent, PropertyMeta property, Object value) {
		if (property.category() == DomainPropertyCategory.Primitive) {
			return PrimitiveUtil.convert(value, property.monotype());
		}
		throw new DomainDrivenException(Language.strings("DomainObjectLoadError", parent.getClass().getName()));
	}

	static DomainObject createInstance(Class<?> objectType, DTObject data) {
		if (data.isEmpty())
			return DomainObject.getEmpty(objectType);

		var obj = constructObject(objectType, data);
		load(obj, data);
		return obj;
	}

	private static DomainObject constructObject(Class<?> objectType, DTObject data) {

		try {

			if (objectType.isAssignableFrom(IDynamicObject.class))
				return (DomainObject) Activator.createInstance(objectType);

			var constructorTip = ConstructorRepositoryImpl.getTip(objectType, false);
			if (constructorTip == null) {
				// 调用无参构造
				return (DomainObject) Activator.createInstance(objectType);
			}
			var constructor = constructorTip.constructor();
			var args = createArguments(constructorTip, data);
			return (DomainObject) constructor.newInstance(args);
		} catch (Exception ex) {
			throw propagate(ex);
		}
	}

	private static Object[] createArguments(ConstructorRepositoryImpl tip, DTObject data) {
		var length = Iterables.size(tip.parameters());

		if (length == 0)
			return ListUtil.emptyObjects();
		Object[] args = new Object[length];
		var prms = tip.parameters();
		var prmsLength = prms.size();
		for (var i = 0; i < prmsLength; i++) {
			var prm = prms.get(i);
			var arg = data.getValue(prm.name());
			args[i] = arg;
		}
		return args;
	}

}
