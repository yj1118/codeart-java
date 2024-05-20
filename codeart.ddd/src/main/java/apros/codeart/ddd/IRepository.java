package apros.codeart.ddd;

public interface IRepository<TRoot extends IAggregateRoot> extends IRepositoryBase {
	/// <summary>
	/// 将对象添加到仓储
	/// </summary>
	/// <param name="obj"></param>
	void add(TRoot obj);

	/// <summary>
	/// 修改对象在仓储中的信息
	/// </summary>
	/// <param name="obj"></param>
	void update(TRoot obj);

	/// <summary>
	/// 从仓储中删除对象
	/// </summary>
	/// <param name="obj"></param>
	void delete(TRoot obj);

	/// <summary>
	/// 根据编号查找对象
	/// </summary>
	/// <param name="id"></param>
	/// <param name="level"></param>
	/// <returns></returns>
	TRoot find(Object id, QueryLevel level);
}
