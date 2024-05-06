package apros.codeart.ddd.dynamic;

import apros.codeart.ddd.IRepository;
import apros.codeart.ddd.QueryLevel;

public interface IDynamicRepository extends IRepository {

	/**
	 * 
	 * 从仓储中根据编号加载动态根对象
	 * 
	 * @param typeName
	 * @param id
	 * @param level
	 * @return
	 */
	DynamicRoot find(String typeName, Object id, QueryLevel level);
}
