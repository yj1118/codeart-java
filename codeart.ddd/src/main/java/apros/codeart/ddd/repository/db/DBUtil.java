package apros.codeart.ddd.repository.db;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataSource;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.DbType;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.StringUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

import static apros.codeart.runtime.Util.propagate;

public final class DBUtil {
    private DBUtil() {
    }


    public static String format(String sql, DataTable table, QueryLevel level) {
        return format(sql, table, null, level);
    }

    /**
     * 由于用户写的对象表达式里的属性是不带postgresql的标识符的，这会执行报错，所以得加上
     *
     * @param sql
     * @return
     */
    public static String format(String sql, DataTable table, String tableAlias, QueryLevel level) {
        return DataSource.getAgent().getSqlFormat().format(sql, table, tableAlias, level);
    }

    public static boolean needInc(DataTable table) {
        var idField = table.idField();
        if (idField != null) {
            var idType = idField.dbType();
            return idType == DbType.Int64 || idType == DbType.Int32 || idType == DbType.Int16;
        }
        return false;
    }

}
