package com.apros.codeart.ddd;

public interface IRepository {
	/// <summary>
	/// 根据编号查找对象
	/// </summary>
	/// <param name="id"></param>
	/// <param name="level"></param>
	/// <returns></returns>
	IAggregateRoot find(Object id, QueryLevel level);

	/// <summary>
	/// 将对象添加到仓储
	/// </summary>
	/// <param name="obj"></param>
	void add(IAggregateRoot obj);

	/// <summary>
	/// 修改对象在仓储中的信息
	/// </summary>
	/// <param name="obj"></param>
	void update(IAggregateRoot obj);

	/// <summary>
	/// 从仓储中删除对象
	/// </summary>
	/// <param name="obj"></param>
	void delete(IAggregateRoot obj);
}
