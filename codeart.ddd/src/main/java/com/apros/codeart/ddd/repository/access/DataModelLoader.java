package com.apros.codeart.ddd.repository.access;

import java.util.function.Function;

import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.IDomainObject;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.util.LazyIndexer;

final class DataModelLoader {

	private DataModelLoader() {

	}

	public static void load(Iterable<Class<? extends IDomainObject>> domainTypes) {
		DataTableLoader.load(domainTypes);

	}

	private static Function<Class<? extends IAggregateRoot>, DataModel> _getModel = LazyIndexer.init((objectType) -> {

		var meta = ObjectMetaLoader.get(objectType);
		var root = DataTableLoader.getRoot(objectType);
		return new DataModel(meta, root);
	});

	public DataModel get(Class<? extends IAggregateRoot> objectType) {
		return _getModel.apply(objectType);
	}

}
