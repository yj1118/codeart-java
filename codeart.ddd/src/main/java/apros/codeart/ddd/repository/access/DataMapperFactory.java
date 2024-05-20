package apros.codeart.ddd.repository.access;

import java.util.function.Function;

import apros.codeart.ddd.IRepositoryBase;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.SafeAccessImpl;

public final class DataMapperFactory {

	private DataMapperFactory() {
	}

	public static IDataMapper create(Class<?> objectType) {
		var meta = ObjectMetaLoader.get(objectType);
		return _getMapper.apply(meta);
	}

	public static IDataMapper create(ObjectMeta meta) {
		return _getMapper.apply(meta);
	}

	private static Function<ObjectMeta, IDataMapper> _getMapper = LazyIndexer.init((meta) -> {
		var mapper = getByRepository(meta);
		if (mapper != null)
			return mapper;
		return DataMapperImpl.Instance;
	});

	/**
	 * 从仓储的定义中得到mapper
	 * 
	 * @param meta
	 * @return
	 */
	private static IDataMapper getByRepository(ObjectMeta meta) {
		var repository = Repository.createByObjectType(meta.objectType());
		if (repository == null)
			return null;
		var dataMapperType = getDataMapperType(repository);
		if (dataMapperType == null)
			return null;
		return SafeAccessImpl.createSingleton(dataMapperType);
	}

	private static Class<? extends IDataMapper> getDataMapperType(IRepositoryBase repository) {
		var ann = repository.getClass().getAnnotation(DataMapper.class);
		if (ann == null)
			return null;
		return ann.type();
	}

}
