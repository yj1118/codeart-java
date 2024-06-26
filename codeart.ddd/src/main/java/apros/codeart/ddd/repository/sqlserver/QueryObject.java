package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryObjectQB;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.ddd.repository.sqlserver.ExpressionHelper;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
class QueryObject extends QueryObjectQB {

    private QueryObject() {
    }

    protected String buildImpl(DataTable target, SqlDefinition definition, QueryLevel level) {

        String objectSql = ExpressionHelper.getObjectSql(target,level, definition);

        StringBuilder sb = new StringBuilder();
        StringUtil.appendLine(sb, objectSql);
        StringUtil.appendFormat(sb, "select distinct %s %s from %sCTE %s",definition.top(),
                definition.getFieldsSql(),
                target.name(),
                definition.order());

        return sb.toString();

//        String objectSql = ExpressionHelper.getObjectSql(target, level, definition);
//
//        return String.format("select %s * from %s %s", definition.top(), objectSql, definition.order());
    }

    public static final QueryObject Instance = new QueryObject();

}
