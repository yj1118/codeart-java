package apros.codeart.ddd.repository.db;

import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.GeneratedField;
import apros.codeart.ddd.repository.access.GetPrimitiveValuesQB;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
public class GetPrimitiveValues extends GetPrimitiveValuesQB {
    private GetPrimitiveValues() {
    }

    @Override
    protected String buildImpl(DataTable table) {
        DataTable middle = table;
        DataTable master = middle.master();
        DataTable root = middle.root();

        var rootId = GeneratedField.RootIdName;

        StringBuilder sql = new StringBuilder();
        if (root.same(master)) {
            StringUtil.appendMessageFormat(sql, "select {1} from {2} where {2}.{0} = @{4} order by {3} asc",
                    SqlStatement.qualifier(rootId),
                    SqlStatement.qualifier(GeneratedField.PrimitiveValueName),
                    SqlStatement.qualifier(middle.name()),
                    SqlStatement.qualifier(GeneratedField.OrderIndexName),
                    rootId);
        } else {
            var masterId = GeneratedField.MasterIdName;

            StringUtil.appendMessageFormat(sql,
                    "select {1} from {2} where {2}.{0} = @{6} and {2}.{3}=@{5} order by {4} asc",
                    SqlStatement.qualifier(rootId),
                    SqlStatement.qualifier(GeneratedField.PrimitiveValueName),
                    SqlStatement.qualifier(middle.name()),
                    SqlStatement.qualifier(masterId),
                    SqlStatement.qualifier(GeneratedField.OrderIndexName),
                    masterId, rootId);
        }
        return sql.toString();
    }

    public static final GetPrimitiveValues Instance = new GetPrimitiveValues();

}