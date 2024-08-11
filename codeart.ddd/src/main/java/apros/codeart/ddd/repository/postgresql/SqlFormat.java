package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.ISqlFormat;
import apros.codeart.ddd.repository.db.DBUtil;
import apros.codeart.ddd.repository.db.SqlFormatBase;

public class SqlFormat extends SqlFormatBase {

    public static final SqlFormat INSTANCE = new SqlFormat();

    private SqlFormat() {
    }

    @Override
    public String format(String sql, DataTable table, String tableAlias, QueryLevel level) {
        return super.format(sql, table, tableAlias, level);
    }
}
