package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryObjectQB;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.ddd.repository.db.DBUtil;
import apros.codeart.ddd.repository.db.ExpressionHelper;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

import java.util.regex.Pattern;

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
//        return String.format("select %s %s from %s %s", definition.top(),
//                definition.getFieldsSql(),
//                objectSql,
//                definition.order());

        String top = definition.top().isEmpty() ? StringUtil.empty() :
                Pattern.compile("top", Pattern.CASE_INSENSITIVE)
                        .matcher(definition.top())
                        .replaceAll("LIMIT");

        String sql = String.format("select %s from %s %s %s",
                definition.getFieldsSql(),
                objectSql,
                definition.order(),
                top);

        return DBUtil.format(sql, target, QueryLevel.NONE);
    }

    public static final QueryObject Instance = new QueryObject();

}
