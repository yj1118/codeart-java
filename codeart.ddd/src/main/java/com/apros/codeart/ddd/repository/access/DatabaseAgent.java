package com.apros.codeart.ddd.repository.access;

import com.apros.codeart.ddd.QueryLevel;

public class DatabaseAgent implements IDatabaseAgent {

	@Override
	public String supplementLock(String sql, QueryLevel level) {
		// 默认情况下，数据库不会通过sql干涉锁（sqlserver例外）
		return sql;
	}

}
