package apros.codeart.ddd.repository.access.internal;

import java.util.function.Function;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.IQueryBuilder;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.TriFunction;

/**
 * 一个简单的表达式缓存，用于缓存查询表达式
 */
public class ExpressionCache {
	private final Function<DataTable, Function<SqlDefinition, Function<QueryLevel, String>>> _getSql;

	public ExpressionCache(TriFunction<DataTable, SqlDefinition, QueryLevel, String> factory) {
		_getSql = LazyIndexer.init((table) -> {
			return LazyIndexer.init((definition) -> {
				return LazyIndexer.init((level) -> {
					return factory.apply(table, definition, level);
				});
			});
		});
	}

	public String getSql(DataTable target, SqlDefinition definition, QueryLevel level) {
		return _getSql.apply(target).apply(definition).apply(level);
	}
}
