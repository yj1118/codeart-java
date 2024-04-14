package com.apros.codeart.ddd.repository.access;

import java.util.function.Function;

import com.apros.codeart.util.LazyIndexer;

public abstract class DecrementAssociatedQB implements IQueryBuilder {

	private Function<DataTable, String> _getSql = LazyIndexer.init((table) -> {
		return buildImpl(table);
	});

	public String build(QueryDescription description) {
		var table = description.table();
		return _getSql.apply(table);
	}

	protected abstract String buildImpl(DataTable table);

}
