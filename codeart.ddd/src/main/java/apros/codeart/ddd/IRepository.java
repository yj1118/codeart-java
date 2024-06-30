package apros.codeart.ddd;

public interface IRepository<TRoot extends IAggregateRoot> extends IRepositoryBase {

    /**
     * 将对象添加到仓储
     *
     * @param obj
     */
    void add(TRoot obj);

    /**
     * 修改对象在仓储中的信息
     *
     * @param obj
     */
    void update(TRoot obj);

    /**
     * 从仓储中删除对象
     *
     * @param obj
     */
    void delete(TRoot obj);


    /**
     * 根据编号查找对象
     *
     * @param id
     * @param level
     * @return
     */
    TRoot find(Object id, QueryLevel level);
}
