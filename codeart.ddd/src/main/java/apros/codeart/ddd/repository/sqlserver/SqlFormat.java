package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.db.DBUtil;
import apros.codeart.ddd.repository.db.SqlFormatBase;

public class SqlFormat extends SqlFormatBase {

    public static final SqlFormat INSTANCE = new SqlFormat();

    private SqlFormat() {
    }

    @Override
    public String format(String sql, DataTable table, String tableAlias, QueryLevel level) {
        var lockCode = LockSql.INSTANCE.get(level);
        sql = LockSql.mapToCCJSqlParser(sql, lockCode);
        sql = super.format(sql, table, tableAlias, level);
        return LockSql.mapToSQLServer(sql, lockCode);
    }
}
