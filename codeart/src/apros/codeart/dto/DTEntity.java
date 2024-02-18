package apros.codeart.dto;

abstract class DTEntity {

	private String _name;

	public String getName() {
		return _name;
	}

	public void setName(String value) {
		_name = value;
	}

	private DTEntity _parent;

	public DTEntity getParent() {
		return _parent;
	}

	public void setParent(DTEntity parent) {
		_parent = parent;
	}

	public abstract DTEntityType getType();

	public abstract DTEntity Clone();

	/**
	 * 根据查找表达式找出dto成员
	 * 
	 * @param query
	 * @return
	 */
	public abstract Iterable<DTEntity> Finds(QueryExpression query);

	/**
	 * @param sequential 是否排序输出代码
	 * @param outputName 是否输出key值
	 * @return
	 */
	public abstract String GetCode(boolean sequential, boolean outputName);

	/**
	 * 输出架构码
	 * 
	 * @param sequential
	 * @param outputName
	 * @return
	 */
	public abstract String GetSchemaCode(boolean sequential, boolean outputName);

}
