package com.apros.codeart.ddd.repository.access;

import java.util.HashMap;
import java.util.Map;

import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.util.SafeAccessImpl;

public abstract class DatabaseAgent implements IDatabaseAgent {

	private Map<Class<? extends IQueryBuilder>, IQueryBuilder> _queryBuilders = new HashMap<>();

	public <T extends IQueryBuilder> void registerQueryBuilder(Class<T> qbClass, IQueryBuilder builder) {
		SafeAccessImpl.checkUp(builder);
		_queryBuilders.put(qbClass, builder);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IQueryBuilder> T getQueryBuilder(Class<T> qbClass) {
		return (T) _queryBuilders.get(qbClass);
	}

	@Override
	public String supplementLock(String sql, QueryLevel level) {
		// 默认情况下，数据库不会通过sql干涉锁（sqlserver例外）
		return sql;
	}

}
