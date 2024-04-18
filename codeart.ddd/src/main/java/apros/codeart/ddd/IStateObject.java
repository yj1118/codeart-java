package apros.codeart.ddd;

/**
 * 状态对象，我们可以明确的知道领域对象的仓储状态
 */
public interface IStateObject {

	/**
	 * 
	 * 是否为脏对象
	 * 
	 * @return
	 */
	boolean isDirty();

	/**
	 * 
	 * 是否为新建对象
	 * 
	 * @return
	 */
	boolean isNew();

	/**
	 * 标记对象为干净对象
	 * 
	 */
	void markClean();

	/**
	 * 标记对象为新建对象
	 */
	void markNew();

	/**
	 * 
	 * 标记对象为脏对象
	 * 
	 */
	void markDirty();

	/**
	 * 保存状态
	 */
	void saveState();

	/**
	 * 从最后一次保存状态中加载
	 */
	void loadState();

}
