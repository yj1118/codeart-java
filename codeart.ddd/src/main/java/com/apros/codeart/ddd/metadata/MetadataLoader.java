package com.apros.codeart.ddd.metadata;

import com.apros.codeart.AppConfig;
import com.apros.codeart.ddd.DomainObject;
import com.apros.codeart.runtime.Activator;

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
		loadByClass();
//		ObjectMetaLoader.load(null);
	}

	/**
	 * 从编码实现的领域对象里，加载对象信息
	 */
	private static void loadByClass() {

		var domainTypes = Activator.getSubTypesOf(DomainObject.class, AppConfig.mergeArchives("subsystem"));
//		List<Type> doTypes = new List<Type>();
//		foreach (var type in types)
//		{
//		    if (!IsMergeDomainType(type))
//		    {
//		        var exists = doTypes.FirstOrDefault((t) =>
//		        {
//		            return t.Name == type.Name;
//		        });
//
//		        if (exists != null)
//		            throw new DomainDrivenException(string.Format("领域对象 {0} 和 {1} 重名", type.FullName, exists.FullName));
//		        doTypes.Add(type);
//		    }
//		}
//		return doTypes;
	}

}
