package apros.codeart.ddd.dynamic;

import apros.codeart.ddd.IRepositoryBase;
import apros.codeart.ddd.QueryLevel;

public interface IDynamicRepository extends IRepositoryBase {

	/**
	 * 
	 * 从仓储中根据编号加载动态根对象
	 * 
	 * @param id
	 * @param level
	 * @return
	 */
	DynamicRoot find(Object id, QueryLevel level);
}
