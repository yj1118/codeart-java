package com.apros.codeart.ddd.repository.access;

import static com.apros.codeart.i18n.Language.strings;

import java.util.function.Function;

import com.apros.codeart.util.LazyIndexer;

public abstract class CreateTableQB implements IQueryBuilder {

	private Function<DataTable, String> _getSql = LazyIndexer.init((table) -> {
		return buildImpl(table);
	});

	public String build(QueryDescription description) {
		var tables = description.tables();
		if (tables.length != 1)
			throw new IllegalArgumentException(strings("codeart.ddd", "CreateTableQBTablesError"));
		return _getSql.apply(description.tables()[0]);
	}

	protected abstract String buildImpl(DataTable table);

}
