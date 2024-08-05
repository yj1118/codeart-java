package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.GeneratedField;
import apros.codeart.ddd.repository.access.UpdateDataVersionQB;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

import java.text.MessageFormat;

@SafeAccess
class UpdateDataVersion extends UpdateDataVersionQB {
    private UpdateDataVersion() {
    }

    @Override
    protected String buildImpl(DataTable table) {

        var sql = new SqlUpdateBuilder();
        sql.setTable(table.name());

        sql.set(StringUtil.format("{0}={0}+1", SqlStatement.qualifier(GeneratedField.DataVersionName)));

        for (var field : table.primaryKeys()) {
            sql.where(field.name());
        }

        return sql.getCommandText();
    }

    public static final UpdateDataVersion Instance = new UpdateDataVersion();
}