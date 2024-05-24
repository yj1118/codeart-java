package apros.codeart.ddd.repository;

import apros.codeart.ddd.dynamic.DynamicRoot;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;

/**
 * 不是基于sql存储的动态对象的仓储
 * 
 * 使用该类作为基类需要自行实现仓储方法
 * 
 */
public abstract class DynamicRepository extends AbstractRepository<DynamicRoot> {

	private String _typeName;

	public DynamicRepository(String typeName) {
		_typeName = typeName;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<? extends DynamicRoot> getRootTypeImpl() {
		return (Class<? extends DynamicRoot>) ObjectMetaLoader.get(_typeName).objectType();
	}

}
