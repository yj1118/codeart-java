package com.apros.codeart.ddd.repository.sqlserver;

import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.repository.access.DataTable;
import com.apros.codeart.ddd.repository.access.QueryObjectQB;
import com.apros.codeart.ddd.repository.access.SqlDefinition;
import com.apros.codeart.util.SafeAccess;

@SafeAccess
class QueryObject extends QueryObjectQB {

	protected String buildImpl(DataTable target, String expression, QueryLevel level) {

		var definition = SqlDefinition.create(expression);

		String objectSql = ExpressionHelper.getObjectSql(target, level, definition);

		return String.format("select {0} * from {1} {2}", definition.top(), objectSql, definition.order());
	}

	public static final QueryObject Instance = new QueryObject();

}
