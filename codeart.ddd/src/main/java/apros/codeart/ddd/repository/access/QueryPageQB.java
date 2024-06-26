package apros.codeart.ddd.repository.access;

import java.util.function.Function;

import apros.codeart.ddd.repository.access.internal.QueryExpression;
import apros.codeart.util.LazyIndexer;

public abstract class QueryPageQB extends QueryExpression {

	public QueryPageQB() {

	}

//	public String build(QueryDescription description) {
//		var target = description.table();
//		String expression = description.getItem("expression");
//		return _getInstance.apply(target).apply(expression);
//	}
//
//	protected abstract String buildImpl(DataTable target, String expression);
//
//	private Function<DataTable, Function<String, String>> _getInstance = LazyIndexer.init((target) -> {
//		return LazyIndexer.init((expression) -> {
//			return buildImpl(target, expression);
//		});
//	});

}
