package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryObjectQB;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
class QueryObject extends QueryObjectQB {

    private QueryObject() {
    }

    @Override
    protected String buildImpl(DataTable target, SqlDefinition definition, QueryLevel level) {

        String objectSql = ExpressionHelper.getObjectSql(target,level, definition);

        StringBuilder sb = new StringBuilder();
        StringUtil.appendLine(sb, objectSql);
        StringUtil.appendFormat(sb, "select distinct %s %s from %s %s",definition.top(),
                definition.getFieldsSql(),
                SqlStatement.qualifier(target.name()),
                definition.order());

        return sb.toString();
    }

    public static final QueryObject Instance = new QueryObject();

}
