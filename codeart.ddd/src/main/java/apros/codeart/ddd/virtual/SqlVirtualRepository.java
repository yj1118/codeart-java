package apros.codeart.ddd.virtual;


import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.access.SqlRepository;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class SqlVirtualRepository extends SqlRepository<VirtualRoot> implements IVirtualRepository {

    private final String _typeName;

    public SqlVirtualRepository(String typeName) {
        _typeName = typeName;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends VirtualRoot> getRootTypeImpl() {
        return (Class<? extends VirtualRoot>) ObjectMetaLoader.get(_typeName).objectType();
    }

}
