package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.QueryLevel;

/**
 * 数据库代理
 */
public interface IDatabaseAgent {

	/**
	 * 
	 * 为sql补充锁提示
	 * 
	 * @param sql
	 * @return
	 */
	String supplementLock(String sql, QueryLevel level);

	/**
	 * 获得自增编号的sql
	 * 
	 * @param tableName 数据库表名
	 * @return
	 */
	String getIncrIdSql(String tableName);

	/**
	 * 能创建索引的字符串的最大长度
	 * 
	 * @return
	 */
	int getStringIndexableMaxLength();

}
