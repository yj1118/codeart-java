package apros.codeart.ddd.repository.access.internal;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryBuilder;
import apros.codeart.ddd.repository.access.QueryDescription;

public abstract class QueryExpression extends QueryBuilder {

	public QueryExpression() {

	}

	public String build(QueryDescription description) {
		var target = description.table();
		String expression = description.getItem("expression");
		QueryLevel level = description.getItem("level");
		if(level == null) level = QueryLevel.NONE;

		var definition = SqlDefinition.create(expression);
		var sql =  _cache.getSql(target, definition, level);
		return definition.process(sql,description.param());
	}

	protected abstract String buildImpl(DataTable target, SqlDefinition definition, QueryLevel level);

	private final ExpressionCache _cache = new ExpressionCache((target, definition, level) -> {
		return buildImpl(target, definition, level);
	});

}
