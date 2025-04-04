package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryCountQB;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.ddd.repository.db.DBUtil;
import apros.codeart.ddd.repository.db.ExpressionHelper;
import apros.codeart.util.SafeAccess;

@SafeAccess
class QueryCount extends QueryCountQB {

    private QueryCount() {
    }

    protected String buildImpl(DataTable target, SqlDefinition definition, QueryLevel level) {

        String objectSql = ExpressionHelper.getObjectSql(target, level, definition, LockSql.INSTANCE);

//        var bottomSql = String.format("select count(DISTINCT %s) from %s", EntityObject.IdPropertyName,
//                SqlStatement.qualifier(target.name()));

//        bottomSql = DBUtil.format(bottomSql, target, QueryLevel.NONE);

        return String.format("select count(*) from %s", objectSql);
    }

    public static final QueryCount Instance = new QueryCount();

}
