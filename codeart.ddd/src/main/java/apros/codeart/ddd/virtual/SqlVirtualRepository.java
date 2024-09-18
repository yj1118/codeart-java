package apros.codeart.ddd.virtual;


import apros.codeart.ddd.MapData;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.Page;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.ddd.repository.access.SqlRepository;
import apros.codeart.dto.DTObject;
import apros.codeart.dto.DTObjects;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

import java.util.ArrayList;

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
