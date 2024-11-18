package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryObjectQB;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.ddd.repository.db.DBUtil;
import apros.codeart.ddd.repository.db.ExpressionHelper;
import apros.codeart.util.SafeAccess;

@SafeAccess
class QueryObject extends QueryObjectQB {

    private QueryObject() {
    }

    protected String buildImpl(DataTable target, SqlDefinition definition, QueryLevel level) {

        // 由于sqlserver的cte写法不支持写实际的表名称，所以要转义
        String tableName = target.name() + "CTE";

        String objectSql = ExpressionHelper.getObjectSql(target, level, definition, LockSql.INSTANCE);


        var bottomSql = String.format("select %s %s from %s %s", definition.top(),
                definition.getFieldsSql(),
                SqlStatement.qualifier(tableName),
                definition.order());

        bottomSql = DBUtil.format(bottomSql, target, tableName, QueryLevel.NONE);

        return String.format("%s%s%s", objectSql, System.lineSeparator(), bottomSql);
    }

    public static final QueryObject Instance = new QueryObject();

}
