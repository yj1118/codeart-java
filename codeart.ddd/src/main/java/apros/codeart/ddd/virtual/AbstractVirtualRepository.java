package apros.codeart.ddd.virtual;

import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.AbstractRepository;

/**
 * 不是基于sql存储的动态对象的仓储
 * <p>
 * 使用该类作为基类需要自行实现仓储方法
 */
public abstract class AbstractVirtualRepository extends AbstractRepository<VirtualRoot> {

    private final String _typeName;

    public AbstractVirtualRepository(String typeName) {
        _typeName = typeName;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends VirtualRoot> getRootTypeImpl() {
        return (Class<? extends VirtualRoot>) ObjectMetaLoader.get(_typeName).objectType();
    }

}
