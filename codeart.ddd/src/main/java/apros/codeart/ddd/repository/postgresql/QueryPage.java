package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryPageQB;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.sqlserver.ExpressionHelper;
import apros.codeart.ddd.repository.sqlserver.SqlPageTemplate;
import apros.codeart.util.SafeAccess;

@SafeAccess
class QueryPage extends QueryPageQB {

    @Override
    protected String buildImpl(DataTable target, String expression) {

        var definition = SqlDefinition.create(expression);

        String objectSql = ExpressionHelper.getObjectSql(target, QueryLevel.NONE, definition);

        var sql = new SqlPageTemplate();
        sql.select("*");
        sql.from(objectSql);// 不需要where，因为GetObjectSql内部已经处理了
        sql.orderBy(definition);
        return sql.toString();
    }

    public final static QueryPage Instance = new QueryPage();

}
