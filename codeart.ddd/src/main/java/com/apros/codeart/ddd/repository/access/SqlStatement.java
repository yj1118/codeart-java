package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.QueryLevel;

final class SqlStatement {
	private SqlStatement() {
	}

	/**
	 * 包装标示限定符
	 * 
	 * @param field
	 * @return
	 */
	public static String qualifier(String field) {
		return DataSource.getAgent().qualifier(field);
	}

	/**
	 * 解开标示限定符
	 * 
	 * @param field
	 * @return
	 */
	public static String unQualifier(String field) {
		return DataSource.getAgent().unQualifier(field);
	}

	/**
	 * 为sql补充锁提示
	 * 
	 * @param sql
	 * @param level
	 * @return
	 */
	public static String supplementLock(String sql, QueryLevel level) {
		return DataSource.getAgent().supplementLock(sql, level);
	}

	/**
	 * 获得自增编号的sql
	 * 
	 * @param tableName
	 * @return
	 */
	public static String getIncrIdSql(String tableName) {
		return DataSource.getAgent().getIncrIdSql(tableName);
	}

}
