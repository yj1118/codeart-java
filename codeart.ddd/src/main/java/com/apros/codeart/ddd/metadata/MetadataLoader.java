package com.apros.codeart.ddd.metadata;

import static com.apros.codeart.i18n.Language.strings;

import java.util.ArrayList;

import com.apros.codeart.AppConfig;
import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.runtime.Activator;
import com.apros.codeart.util.ListUtil;

public final class MetadataLoader {
	private MetadataLoader() {
	}

	private static boolean loaded = false;

	/**
	 * 加载所有领域对象的元数据
	 */
	public static void load() {
		if (loaded)
			return;
		loaded = true;

		// 在这里找出所有定义的领域对象
		localNative();
	}

	/**
	 * 加载原生的领域对象信息
	 */
	private static void localNative() {
		var domainTypes = findNativeDomainTypes();
		for (var domainType : domainTypes) {
			ObjectMetaLoader.load(domainType);
		}
	}

	private static Iterable<Class<?>> findNativeDomainTypes() {
		var findedTypes = Activator.getSubTypesOf(DomainObject.class, AppConfig.mergeArchives("subsystem"));
		ArrayList<Class<?>> domainTypes = new ArrayList<>();
		for (var findedType : findedTypes) {
			if (ListUtil.contains(findedTypes, null))

				if (!ObjectMeta.isMergeDomainType(findedType)) {
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
		return domainTypes;
	}

}
