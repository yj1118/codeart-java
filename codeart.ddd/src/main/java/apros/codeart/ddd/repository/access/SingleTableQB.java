package apros.codeart.ddd.repository.access;

import static apros.codeart.i18n.Language.strings;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;

public abstract class SingleTableQB implements IQueryBuilder {

	protected boolean enableCache() {
		return true;
	}

	private Function<DataTable, String> _getSql = LazyIndexer.init((table) -> {
		return buildImpl(table);
	});

	public String build(QueryDescription description) {
		var table = description.table();
		if (table == null)
			throw new IllegalArgumentException(strings("apros.codeart.ddd", "SingleTableQBError"));
		if (this.enableCache())
			return _getSql.apply(table);
		return buildImpl(table);
	}

	protected abstract String buildImpl(DataTable table);

}
