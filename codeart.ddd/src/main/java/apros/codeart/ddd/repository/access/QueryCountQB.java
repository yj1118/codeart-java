package apros.codeart.ddd.repository.access;

import apros.codeart.ddd.QueryLevel;

public abstract class QueryCountQB implements IQueryBuilder {

	public QueryCountQB() {

	}

	public String build(QueryDescription description) {
		var target = description.table();
		String expression = description.getItem("expression");
		QueryLevel level = description.getItem("level");
		return _cache.getSql(target, expression, level);
	}

	protected abstract String buildImpl(DataTable target, String expression, QueryLevel level);

	private ExpressionCache<QueryCountQB> _cache = new ExpressionCache<QueryCountQB>((target, expression, level) -> {
		return buildImpl(target, expression, level);
	});

}
