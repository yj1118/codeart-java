package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryDescription;
import apros.codeart.ddd.repository.access.QueryPageQB;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.ddd.repository.db.DBUtil;
import apros.codeart.ddd.repository.postgresql.SqlPageTemplate;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.SafeAccess;

import java.text.MessageFormat;
import java.util.function.Function;

@SafeAccess
class QueryPage extends QueryPageQB {


    private static final Function<DataTable, Function<SqlDefinition, SqlPageTemplate>> _getTemplate = LazyIndexer.init((table) -> {
        return LazyIndexer.init((definition) -> {
            String objectSql = ExpressionHelper.getObjectSql(table, QueryLevel.NONE, definition);

            var sql = new SqlPageTemplate(table);
            sql.select(definition.getFieldsSql());
            sql.from(SqlStatement.qualifier(table.name()));
            sql.orderBy(definition);

            sql.build(objectSql);

            return sql;
        });
    });

    @Override
    public String build(QueryDescription description) {

        var target = description.table();
        String expression = description.getItem("expression");
        var definition = SqlDefinition.create(expression);

        var template = _getTemplate.apply(target).apply(definition);
        var param = description.param();

        var pageIndex = (int) param.get("pageIndex");
        var pageSize = (int) param.get("pageSize");

        var sql = template.getCode(pageIndex, pageSize);
        return definition.process(sql, description.param());
    }

    public final static QueryPage Instance = new QueryPage();

}
