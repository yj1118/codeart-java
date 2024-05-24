package apros.codeart.ddd.metadata.internal;

import static apros.codeart.i18n.Language.strings;

import java.util.ArrayList;

import apros.codeart.App;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.runtime.Activator;
import apros.codeart.util.ListUtil;

public final class MetadataLoader {
	private MetadataLoader() {
	}

	private static boolean loaded = false;

	private static Iterable<Class<? extends IDomainObject>> _domainTypes;

	public static Iterable<Class<? extends IDomainObject>> getDomainTypes() {
		if (!loaded)
			throw new IllegalArgumentException(strings("codeart.ddd", "MetadataNotInitialized"));
		return _domainTypes;
	}

	/**
	 * 加载所有领域对象的元数据
	 */
	public static Iterable<Class<? extends IDomainObject>> load() {
		if (loaded)
			return _domainTypes;
		loaded = true;

		// 在这里找出所有定义的领域对象
		_domainTypes = findDomainTypes();

		// 加载
		ObjectMetaLoader.load(_domainTypes);

		return _domainTypes;
	}

	/**
	 * @return
	 */
	private static Iterable<Class<? extends IDomainObject>> findDomainTypes() {
		var findedTypes = Activator.getSubTypesOf(DomainObject.class, App.archives());
		ArrayList<Class<? extends IDomainObject>> domainTypes = new ArrayList<>(_registerItems);
		for (var findedType : findedTypes) {

			if (!ObjectMeta.isMergeDomainType(findedType) && !isEmptyType(findedType)) {

				// 如果找到的是注册的类型，直接忽略，因为已经包含了注册类型
				if (ListUtil.contains(_registerItems, (t) -> t == findedType))
					continue;

				// 非注册的类型要判断是否重名
				var exist = ListUtil.find(domainTypes, (t) -> {
					return t.getSimpleName().equalsIgnoreCase(findedType.getSimpleName());
				});
				if (exist != null) {
					throw new IllegalArgumentException(
							strings("codeart.ddd", "DomainSameName", findedType.getName(), exist.getName()));
				}
				domainTypes.add(findedType);
			}
		}

		domainTypes.trimToSize();

		return domainTypes;
	}

	private static boolean isEmptyType(Class<?> objectType) {
		var name = objectType.getName();
		return name.indexOf("$") > -1 && name.endsWith("Empty");
	}

	private static ArrayList<Class<? extends IDomainObject>> _registerItems = new ArrayList<Class<? extends IDomainObject>>();

	/**
	 * 
	 * 注册领域对象类型，这往往用于生成动态领域对象时的需求
	 * 
	 * @param domainType
	 */
	public static void register(Class<? extends IDomainObject> domainType) {
		// 不能重复注册
		var exist = ListUtil.find(_registerItems, (t) -> {
			return t.getSimpleName().equalsIgnoreCase(domainType.getSimpleName());
		});
		if (exist != null) {
			throw new IllegalArgumentException(
					strings("codeart.ddd", "DomainSameName", domainType.getName(), exist.getName()));
		}
		_registerItems.add(domainType);
	}

}
