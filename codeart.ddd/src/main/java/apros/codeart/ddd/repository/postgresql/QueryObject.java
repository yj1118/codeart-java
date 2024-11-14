package apros.codeart.ddd.repository.postgresql;

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

    @Override
    protected String buildImpl(DataTable target, SqlDefinition definition, QueryLevel level) {

        String objectSql = ExpressionHelper.getObjectSql(target, level, definition, LockSql.INSTANCE);

//        var bottomSql = String.format("select %s %s from %s %s", definition.top(),
//                definition.getFieldsSql(),
//                SqlStatement.qualifier(target.name()),
//                definition.order());
//
//        bottomSql = DBUtil.format(bottomSql, target, QueryLevel.NONE);
//
//        return String.format("%s%s%s", objectSql, System.lineSeparator(), bottomSql);
//
        return String.format("select %s %s from %s %s", definition.top(),
                definition.getFieldsSql(),
                objectSql,
                definition.order());
    }

    public static final QueryObject Instance = new QueryObject();

}
