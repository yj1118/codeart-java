package com.apros.codeart.ddd.repository.access;

import java.util.function.Function;

import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.util.LazyIndexer;

final class DataModelLoader {

	private DataModelLoader() {

	}

	public static void load(Iterable<Class<? extends IDomainObject>> doTypes) {
		DataTableLoader.load(doTypes);
		DataTableLoader.disposeTemp();
	}

	private static Function<Class<? extends IDomainObject>, DataModel> _getModel = LazyIndexer.init((objectType) -> {

		var meta = ObjectMetaLoader.get(objectType);
		var root = DataTableLoader.getRoot(objectType);
		return new DataModel(meta, root);
	});

	public static DataModel get(Class<? extends IDomainObject> objectType) {
		return _getModel.apply(objectType);
	}

}
