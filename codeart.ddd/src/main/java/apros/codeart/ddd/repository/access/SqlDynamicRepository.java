package apros.codeart.ddd.repository.access;

import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.dynamic.IDynamicRepository;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class SqlDynamicRepository extends SqlRepository<DynamicRoot> implements IDynamicRepository {

	private Class<? extends DynamicRoot> _rootType;

	@SuppressWarnings("unchecked")
	public SqlDynamicRepository(String typeName) {
		_rootType = (Class<? extends DynamicRoot>) ObjectMetaLoader.get(typeName).objectType();
	}

	@Override
	protected Class<? extends DynamicRoot> getRootType() {
		return _rootType;
	}
}
