package apros.codeart.ddd;

import apros.codeart.TestSupport;
import apros.codeart.ddd.repository.Page;
import apros.codeart.ddd.virtual.VirtualRoot;
import apros.codeart.dto.DTObject;

public interface IRepositoryBase {
    /**
     * 根据编号查找对象
     *
     * @param id
     * @param level
     * @return
     */
    IAggregateRoot findRoot(Object id, QueryLevel level);

    /**
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

    /**
     * 仓储的初始化操作
     */
    void init();


    /**
     * 清理操作，主要是为了提供给测试使用
     */
    @TestSupport
    void clearUp();


    //#region 动态查询的支持

    DTObject findPage(int pageIndex, int pageSize, DTObject config);

    //#endregion


}
