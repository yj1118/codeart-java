package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.IAggregateRoot;
import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.dynamic.DynamicRoot;
import com.apros.codeart.ddd.dynamic.IDynamicRepository;
import com.apros.codeart.util.SafeAccess;

@SafeAccess
public class SqlDynamicRepository implements IDynamicRepository {

	private SqlDynamicRepository() {
	}

	public <T extends DynamicRoot> T find(Class<T> rootType, Object id, QueryLevel level) {
		var model = DataModelLoader.get(rootType);
		return model.querySingle(id, level);
	}

	/**
	 * 向仓储中添加动态根对象
	 */
	public <T extends DynamicRoot> void add(Class<T> rootType, T obj) {
		var model = DataModelLoader.get(rootType);
		model.insert(obj);
	}

	/// <summary>
	/// 修改仓储中的根对象
	/// </summary>
	/// <param name="define"></param>
	/// <param name="obj"></param>
	public <T extends DynamicRoot> void update(Class<T> rootType, T obj) {
		var model = DataModelLoader.get(rootType);
		model.update(obj);
	}

	/**
	 * 移除仓储中的根对象
	 */
	public <T extends DynamicRoot> void delete(Class<T> rootType, T obj) {
		var model = DataModelLoader.get(rootType);
		model.delete(obj);
	}

	public static final SqlDynamicRepository Instance = new SqlDynamicRepository();

	@Override
	public IAggregateRoot findRoot(Object id, QueryLevel level) {
		throw new UnsupportedOperationException("SqlDynamicRepository.findRoot(object id, QueryLevel level)");
	}

	@Override
	public void addRoot(IAggregateRoot obj) {
		throw new UnsupportedOperationException("SqlDynamicRepository.addRoot(IAggregateRoot obj)");

	}

	@Override
	public void updateRoot(IAggregateRoot obj) {
		throw new UnsupportedOperationException("SqlDynamicRepository.updateRoot(IAggregateRoot obj)");

	}

	@Override
	public void deleteRoot(IAggregateRoot obj) {
		throw new UnsupportedOperationException("SqlDynamicRepository.deleteRoot(IAggregateRoot obj)");
	}

}
