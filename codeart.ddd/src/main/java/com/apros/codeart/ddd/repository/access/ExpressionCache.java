package com.apros.codeart.ddd.repository.access;

import java.util.function.Function;

import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.util.LazyIndexer;
import com.apros.codeart.util.TriFunction;

/**
 * 一个简单的表达式缓存，用于缓存查询表达式
 */
class ExpressionCache<T extends IQueryBuilder> {
	private Function<DataTable, Function<String, Function<QueryLevel, String>>> _getSql;

	public ExpressionCache(TriFunction<DataTable, String, QueryLevel, String> factory) {
		_getSql = LazyIndexer.init((table) -> {
			return LazyIndexer.init((expression) -> {
				return LazyIndexer.init((level) -> {
					return factory.apply(table, expression, level);
				});
			});
		});
	}

	public String getSql(DataTable target, String expression, QueryLevel level) {
		return _getSql.apply(target).apply(expression).apply(level);
	}
}
