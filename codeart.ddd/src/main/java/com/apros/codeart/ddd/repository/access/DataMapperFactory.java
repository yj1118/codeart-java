package com.apros.codeart.ddd.repository.access;

import java.util.function.Function;

import com.apros.codeart.ddd.IRepository;
import com.apros.codeart.ddd.metadata.ObjectMeta;
import com.apros.codeart.ddd.metadata.ObjectMetaLoader;
import com.apros.codeart.ddd.repository.Repository;
import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.SafeAccessImpl;

final class DataMapperFactory {

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

	private static Class<? extends IDataMapper> getDataMapperType(IRepository repository) {
		var ann = repository.getClass().getAnnotation(DataMapper.class);
		if (ann == null)
			return null;
		return ann.type();
	}

}
