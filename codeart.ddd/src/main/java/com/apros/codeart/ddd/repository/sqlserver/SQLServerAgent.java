package com.apros.codeart.ddd.repository.sqlserver;

import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.repository.access.DatabaseAgent;
import com.apros.codeart.ddd.repository.access.IDatabaseAgent;

public class SQLServerAgent extends DatabaseAgent {

	private SQLServerAgent() {
	}

	@Override
	public String supplementLock(String sql, QueryLevel level) {
		return LockSql.get(sql, level);
	}

	public static final IDatabaseAgent Instance = new DatabaseAgent();

}
