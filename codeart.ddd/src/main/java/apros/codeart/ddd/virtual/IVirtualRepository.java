package apros.codeart.ddd.virtual;

import apros.codeart.ddd.IRepositoryBase;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.Page;
import apros.codeart.dto.DTObject;

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
