package com.apros.codeart.ddd.metadata;

import static com.apros.codeart.i18n.Language.strings;
import static com.apros.codeart.runtime.Util.propagate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.apros.codeart.ddd.DomainDrivenException;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.i18n.Language;
import com.apros.codeart.runtime.FieldUtil;
import com.apros.codeart.runtime.TypeUtil;

public final class ObjectMetaLoader {

	private ObjectMetaLoader() {
	}

	private static Map<String, ObjectMeta> _metas = new HashMap<>();

	private static ObjectMeta obtain(String objectName, Supplier<ObjectMeta> create) {
		var meta = _metas.get(objectName);
		if (meta == null) {
			synchronized (_metas) {
				meta = _metas.get(objectName);
				if (meta == null) {
					meta = create.get();
					_metas.put(objectName, meta);
				}
			}
		}
		return meta;
	}

	/**
	 * 
	 * 
	 * 
	 * @param objectName
	 * @return
	 */
	public static ObjectMeta get(String objectName) {
		// 由于是初始化期间执行的obtain，初始化完毕后，才会执行get，所以不会有并发问题,因此没有加锁
		// 初始化期间是单线程的，尚未接受外界请求，所以也是安全的
		var obj = _metas.get(objectName);
		if (obj == null) {
			throw new DomainDrivenException(Language.strings("NotFoundDomainObject", objectName));
		}
		return obj;
	}

	public static ObjectMeta get(Class<?> objectType) {
		return get(objectType.getSimpleName());
	}

	public static ObjectMeta tryGet(Class<?> objectType) {
		if (ObjectMeta.isDomainObject(objectType))
			return get(objectType);

		return null; // 非领域类型是没有元数据的
	}

	private static ObjectMeta obtain(Class<?> objectType) {
		var objectName = objectType.getSimpleName();
		return obtain(objectName, () -> {
			return createByClass(objectType);
		});
	}

	static ObjectMeta load(Class<?> objectType) {
		var meta = obtain(objectType);
		staticConstructor(objectType);
		return meta;
	}

	/**
	 * 通过调用一个静态成员，来模拟触发领域对象类型的静态构造函数
	 * 
	 * 如果类型没有静态成员，那么就算不触发静态构造也不会影响，因为不需要注入领域属性和空对象
	 * 
	 * @param objectType
	 */
	private static void staticConstructor(Class<?> objectType) {
		try {
			// 当new一个实例时，静态构造会从基类依次执行,但是如果仅仅只是获得子类的静态成员，那么是不会触发基类的静态构造函数的
			// 因此，我们需要从基类开始，依次调用
			var types = TypeUtil.getInheriteds(objectType);

			for (var type : types) {
				if (!type.isAssignableFrom(IDomainObject.class))
					continue;

				var field = FieldUtil.firstStaticField(type);
				if (field != null)
					field.get(null);// 获取一次静态值，触发静态构造
			}

			// 再触发自身
			{
				var field = FieldUtil.firstStaticField(objectType);
				if (field != null)
					field.get(null);
			}

		} catch (Exception e) {
			throw propagate(e);
		}

	}

	private static ObjectMeta createByClass(Class<?> objectType) {
		var name = objectType.getSimpleName();
		var category = getCategory(objectType);

		return new ObjectMeta(name, objectType, category);
	}

	private static DomainObjectCategory getCategory(Class<?> objectType) {
		if (ObjectMeta.isAggregateRoot(objectType))
			return DomainObjectCategory.AggregateRoot;

		if (ObjectMeta.isEntityObject(objectType))
			return DomainObjectCategory.EntityObject;

		if (ObjectMeta.isValueObject(objectType))
			return DomainObjectCategory.ValueObject;

		throw new IllegalArgumentException(strings("TypeMismatch"));
	}
}
