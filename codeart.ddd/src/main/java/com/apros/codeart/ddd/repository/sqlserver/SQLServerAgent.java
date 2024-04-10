package com.apros.codeart.ddd.repository.sqlserver;

import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.repository.access.DatabaseAgent;
import com.apros.codeart.ddd.repository.access.IDatabaseAgent;
import com.apros.codeart.ddd.repository.access.IQueryBuilder;

public class SQLServerAgent extends DatabaseAgent {

	private SQLServerAgent() {
	}

	@Override
	public String supplementLock(String sql, QueryLevel level) {
		return LockSql.get(sql, level);
	}

	public static final IDatabaseAgent Instance = new SQLServerAgent();

	@Override
	public String getIncrIdSql(String tableName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStringIndexableMaxLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T extends IQueryBuilder> T getQueryBuilder(Class<T> qbClass) {
		// TODO Auto-generated method stub

	}
}
