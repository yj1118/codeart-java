package apros.codeart.ddd.repository.access;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;

public abstract class ClearTableQB extends QueryBuilder {

	private Function<DataTable, String> _getSql = LazyIndexer.init((table) -> {
		return buildImpl(table);
	});

	public String build(QueryDescription description) {
		var table = description.table();
		return _getSql.apply(table);
	}

	protected abstract String buildImpl(DataTable table);

}
