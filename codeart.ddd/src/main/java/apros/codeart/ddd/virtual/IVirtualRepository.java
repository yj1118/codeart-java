package apros.codeart.ddd.virtual;

import apros.codeart.ddd.IRepositoryBase;
import apros.codeart.ddd.QueryLevel;

public interface IVirtualRepository extends IRepositoryBase {

    /**
     * 从仓储中根据编号加载动态根对象
     *
     * @param id
     * @param level
     * @return
     */
    VirtualRoot find(Object id, QueryLevel level);
}
