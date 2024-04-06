package com.apros.codeart.ddd.repository.access;

import java.util.HashMap;
import java.util.Map;

import com.apros.codeart.ddd.metadata.ObjectMetaLoader;

final class DataModelLoader {

	private DataModelLoader() {

	}

	public static void load(Iterable<Class<?>> domainTypes) {

		// 为了防止循环引用导致的死循环，要先预加载数据表定义
		for (var domainType : domainTypes) {
			obtain(domainType);
		}

		// 再加载完整定义
		for (var domainType : domainTypes) {
			staticConstructor(domainType);
		}
	}

	private static Map<Class<?>, DataModel> _models = new HashMap<>();

	static DataModel obtain(Class<?> objectType) {
		var model = _models.get(objectType);
		if (model == null) {
			model = create(objectType);
			_models.put(objectType, model);
		}
		return model;
	}

	static DataModel create(Class<?> objectType) {
		var objectMeta = ObjectMetaLoader.get(objectType);
		var mapper = DataMapperFactory.create(objectMeta);

		var objectFields = mapper.GetObjectFields(objectType, false);
		var root = DataTable.create(objectType, objectFields);

		return new DataModel(objectType, root);
	}

}
