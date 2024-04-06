package com.apros.codeart.ddd.repository.access;

class DataTable {

	private Class<?> _objectType;

	/**
	 * 
	 * 表对应对实体类型，注意中间表对应的是集合类型
	 * 
	 * @return
	 */
	public Class<?> objectType() {
		return _objectType;
	}

	private DataTableType _type;

	/**
	 * 
	 * 表类型
	 * 
	 * @return
	 */
	public DataTableType type() {
		return _type;
	}

	public String _name;

	/**
	 * 表名称
	 * 
	 * @return
	 */
	public String name() {
		return _name;
	}

	DataTable(Class<?> objectType, DataTableType type, String name) {
		_objectType = objectType;
		_type = type;
		_name = name;
	}
}
