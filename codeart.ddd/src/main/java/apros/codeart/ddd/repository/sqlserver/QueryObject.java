package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryObjectQB;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.util.SafeAccess;

@SafeAccess
class QueryObject extends QueryObjectQB {

    private QueryObject() {
    }

    protected String buildImpl(DataTable target, String expression, QueryLevel level) {

        var definition = SqlDefinition.create(expression);

        String objectSql = ExpressionHelper.getObjectSql(target, level, definition);

        return String.format("select %s * from %s %s", definition.top(), objectSql, definition.order());
    }

    public static final QueryObject Instance = new QueryObject();

}
