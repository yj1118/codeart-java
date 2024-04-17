package com.apros.codeart.ddd.repository.sqlserver;

import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.repository.access.DataTable;
import com.apros.codeart.ddd.repository.access.QueryCountQB;
import com.apros.codeart.ddd.repository.access.SqlDefinition;
import com.apros.codeart.util.SafeAccess;

@SafeAccess
class QueryCount extends QueryCountQB {

	private QueryCount() {
	}

	protected String buildImpl(DataTable target, String expression, QueryLevel level) {

		var definition = SqlDefinition.create(expression);

		String objectSql = ExpressionHelper.getObjectSql(target, level, definition);

		return String.format("select count({0}) from {1}", EntityObject.IdPropertyName, objectSql);
	}

	public static final QueryCount Instance = new QueryCount();

}
