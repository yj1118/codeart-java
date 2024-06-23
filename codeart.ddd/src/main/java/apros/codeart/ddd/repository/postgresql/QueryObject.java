package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryObjectQB;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.ddd.repository.postgresql.ExpressionHelper;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
class QueryObject extends QueryObjectQB {

    private QueryObject() {
    }

    protected String buildImpl(DataTable target, String expression, QueryLevel level) {

        var definition = SqlDefinition.create(expression);

        String objectSql = ExpressionHelper.getObjectSql(target, level, definition);

        StringBuilder sb = new StringBuilder();
        StringUtil.appendLine(sb, objectSql);
        StringUtil.appendFormat(sb, "select %s * from %s %s",definition.top(), SqlStatement.qualifier(target.name()),definition.order());

        return sb.toString();
    }

    public static final QueryObject Instance = new QueryObject();

}
