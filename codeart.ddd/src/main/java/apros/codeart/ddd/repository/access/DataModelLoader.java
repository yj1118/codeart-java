package apros.codeart.ddd.repository.access;

import java.util.function.Function;

import apros.codeart.ddd.IDomainObject;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.util.LazyIndexer;

public final class DataModelLoader {

	private DataModelLoader() {

	}

	public static void load(Iterable<Class<? extends IDomainObject>> doTypes) {
		DataTableLoader.load(doTypes);
		DataTableLoader.disposeTemp();
//		//初始化数据源，持久层可能要做一些初始化操作
//		DataSource.init();
	}

	private static Function<Class<? extends IDomainObject>, DataModel> _getModel = LazyIndexer.init((objectType) -> {

		var meta = ObjectMetaLoader.get(objectType);
		var root = DataTableLoader.getRoot(objectType);
		return new DataModel(meta, root);
	});

	static DataModel get(Class<? extends IDomainObject> objectType) {
		return _getModel.apply(objectType);
	}

}
