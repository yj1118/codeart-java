package apros.codeart.ddd.repository.access;

import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.dynamic.IDynamicRepository;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class SqlDynamicRepository extends SqlRepository<DynamicRoot> implements IDynamicRepository {

	private String _typeName;

	public SqlDynamicRepository(String typeName) {
		_typeName = typeName;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<? extends DynamicRoot> getRootTypeImpl() {
		return (Class<? extends DynamicRoot>) ObjectMetaLoader.get(_typeName).objectType();
	}

}
