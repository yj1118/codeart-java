package com.apros.codeart.ddd.repository.access;

import java.util.function.Function;

import com.apros.codeart.ddd.metadata.ObjectMeta;
import com.apros.codeart.ddd.repository.Repository;

final class DataMapperFactory {

	private DataMapperFactory() {
	}

	public static IDataMapper create(ObjectMeta meta) {
		return _getMapper.apply(meta);
	}

	private static Function<Class<?>, IDataMapper> _getMapper = LazyIndexer.init((objectType)->
	{
	    return getByRepository(objectType) ?? DataMapper.Instance;
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
		var dataMapperType = DataMapperAttribute.GetDataMapperType(repository);
		if (dataMapperType == null)
			return null;
		return SafeAccessAttribute.CreateSingleton < IDataMapper > (dataMapperType);
	}

}
