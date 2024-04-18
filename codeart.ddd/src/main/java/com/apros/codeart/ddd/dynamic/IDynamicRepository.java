package com.apros.codeart.ddd.dynamic;

import com.apros.codeart.ddd.IRepository;
import com.apros.codeart.ddd.QueryLevel;

public interface IDynamicRepository extends IRepository {

	/**
	 * 
	 * 从仓储中根据编号加载动态根对象
	 * 
	 * @param <T>
	 * @param rootType
	 * @param id
	 * @param level
	 * @return
	 */
	<T extends DynamicRoot> T find(Class<T> rootType, Object id, QueryLevel level);

	/// <summary>
	/// 向仓储中添加动态根对象
	/// </summary>
	/// <param name="define"></param>
	/// <param name="obj"></param>
	<T extends DynamicRoot> void add(Class<T> rootType, T obj);

	/// <summary>
	/// 修改仓储中的根对象
	/// </summary>
	/// <param name="define"></param>
	/// <param name="obj"></param>
	<T extends DynamicRoot> void update(Class<T> rootType, T obj);

	/// <summary>
	/// 移除仓储中的根对象
	/// </summary>
	/// <param name="define"></param>
	/// <param name="obj"></param>
	<T extends DynamicRoot> void delete(Class<T> rootType, T obj);

}
