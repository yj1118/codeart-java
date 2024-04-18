package apros.codeart.ddd;

public interface IRepository {
	/**
	 * 
	 * 根据编号查找对象
	 * 
	 * @param id
	 * @param level
	 * @return
	 */
	IAggregateRoot findRoot(Object id, QueryLevel level);

	/**
	 * 
	 * 将对象添加到仓储
	 * 
	 * @param obj
	 */
	void addRoot(IAggregateRoot obj);

	/**
	 * 修改对象在仓储中的信息
	 * 
	 * @param obj
	 */
	void updateRoot(IAggregateRoot obj);

	/**
	 * 从仓储中删除对象
	 * 
	 * @param obj
	 */
	void deleteRoot(IAggregateRoot obj);
}
