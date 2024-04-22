package apros.codeart.ddd.dynamic;

import apros.codeart.dto.DTObject;

public interface IDynamicObject {

	/**
	 * 从dto中加载数据
	 * 
	 * @param data
	 */
	void load(DTObject data);

	/**
	 * 得到动态对象内部所有的动态根类型的成员对象（不包括当前对象自身，也不包括空的根对象）
	 * 
	 * @return
	 */
	<T extends DynamicRoot> Iterable<T> getRefRoots();
}
