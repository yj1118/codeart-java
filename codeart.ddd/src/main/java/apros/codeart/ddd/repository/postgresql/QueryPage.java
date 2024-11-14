package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.QueryDescription;
import apros.codeart.ddd.repository.access.QueryPageCode;
import apros.codeart.ddd.repository.access.QueryPageQB;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.ddd.repository.db.ExpressionHelper;
import apros.codeart.ddd.repository.db.SqlQueryPageCompiler;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

import java.util.function.Function;

@SafeAccess
class QueryPage extends QueryPageQB {


    private static final Function<DataTable, Function<SqlDefinition, QueryPageCode>> _getCode = LazyIndexer.init((table) -> {
        return LazyIndexer.init((definition) -> {
            String tableSql = ExpressionHelper.getTableSql(table, QueryLevel.NONE, definition, LockSql.INSTANCE);

            String orderSql = definition.orderHint();

            if (StringUtil.isNullOrEmpty(orderSql)) {
                orderSql = String.format("%s ASC", EntityObject.IdPropertyName);
            } else {
                var order = definition.columns().order();
                // 对于翻页列表，我们需要保证排序的唯一性，时间有时候不能保证，所以我们会主动追加根据id排序
                if (!ListUtil.contains(order, (t) -> t.equalsIgnoreCase(EntityObject.IdPropertyName)))
                    orderSql = String.format("%s,%s ASC", orderSql, EntityObject.IdPropertyName);
            }
            var keyField = String.format("%s.%s", SqlStatement.qualifier(table.name()), SqlStatement.qualifier(table.idField().name()));
            String selectSql = String.format("DISTINCT ON (%s) %s", keyField, ExpressionHelper.getSelectFieldsSql(table, definition));
            var code = new QueryPageCode(selectSql, tableSql, orderSql);
            code.bind(table);
            return code;
        });
    });

    @Override
    public String build(QueryDescription description) {

        var target = description.table();
        String expression = description.getItem("expression");
        var definition = SqlDefinition.create(expression);

        var code = _getCode.apply(target).apply(definition);
        var param = description.param();

        var pageIndex = (int) param.get("pageIndex");
        var pageSize = (int) param.get("pageSize");

        var compiler = SqlQueryPageCompiler.INSTANCE;
        var pageSql = compiler.buildPage(code, pageIndex, pageSize);
        return definition.process(pageSql, description.param());
    }

    public final static QueryPage Instance = new QueryPage();

}
