package com.apros.codeart.ddd.repository.access;

public interface IQueryBuilder {

	/**
	 * 
	 * 构建执行语句，在这个过程中有可能改变param的值
	 * 
	 * @param param
	 * @param tables
	 * @return
	 */
	String build(QueryDescription description);
}
