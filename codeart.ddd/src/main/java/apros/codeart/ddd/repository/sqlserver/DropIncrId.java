package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.DropIncrIdQB;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
class DropIncrId extends DropIncrIdQB {
    private DropIncrId() {
    }

    @Override
    protected String buildImpl(DataTable table) {
        StringBuilder sql = new StringBuilder();

        StringUtil.appendFormat(sql, "if ISNULL(object_id(N'[%sIncrement]'),'') > 0", table.name());
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql, "begin");
        StringUtil.appendFormat(sql, "DROP TABLE [%sIncrement]", table.name());
        StringUtil.appendLine(sql);
        sql.append("end");
        return sql.toString();
    }

    public static final DropIncrId Instance = new DropIncrId();

}
