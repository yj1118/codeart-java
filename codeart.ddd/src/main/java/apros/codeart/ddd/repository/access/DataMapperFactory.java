package apros.codeart.ddd.repository.access;

import java.util.function.Function;

import apros.codeart.ddd.IAggregateRoot;
import apros.codeart.ddd.IRepositoryBase;
import apros.codeart.ddd.metadata.ObjectMeta;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.SafeAccessImpl;

public final class DataMapperFactory {

    private DataMapperFactory() {
    }

    public static IDataMapper create(Class<? extends IAggregateRoot> objectType) {
        return _getMapper.apply(objectType);
    }

//    public static IDataMapper create(ObjectMeta meta) {
//        return _getMapper.apply(meta);
//    }

    private static final Function<Class<? extends IAggregateRoot>, IDataMapper> _getMapper = LazyIndexer.init((objectType) -> {
        var mapper = getByRepository(objectType);
        if (mapper != null)
            return mapper;
        return DataMapperImpl.Instance;
    });

    /**
     * 从仓储的定义中得到mapper
     */
    private static IDataMapper getByRepository(Class<? extends IAggregateRoot> objectType) {
        var repository = Repository.createByObjectType(objectType);
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
