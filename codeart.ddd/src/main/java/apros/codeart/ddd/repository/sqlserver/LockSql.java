package apros.codeart.ddd.repository.sqlserver;

import java.util.function.Function;
import java.util.regex.Pattern;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.db.ILockSql;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

final class LockSql implements ILockSql {

    private LockSql() {
    }

    public String get(QueryLevel level) {
        switch (level.code()) {
            case QueryLevel.ShareCode:
                return " with(holdlock) ";
            case QueryLevel.SingleCode:
                return " with(xlock,rowlock) ";
            case QueryLevel.HoldCode:
                return " with(xlock,holdlock) ";
            default:
                return " with(nolock) "; // None和Mirror 都是无锁模式
        }
    }

    public static final LockSql INSTANCE = new LockSql();


    /**
     * JSqlParser 可以识别 WITH (NOLOCK)锁提示，但是无法识别其他的锁提示，因此用这个占位，然后恢复真正的锁代码
     */
    private static final String placeHolder = " WITH (NOLOCK) ";

    public static String mapToCCJSqlParser(String sql, String lockCode) {
        if (!StringUtil.isNullOrEmpty(lockCode))
            sql = sql.replace(lockCode, placeHolder);
        return sql;
    }

    public static String mapToSQLServer(String sql, String lockCode) {
        if (!StringUtil.isNullOrEmpty(lockCode))
            sql = sql.replace(placeHolder, lockCode);
        return sql;
    }


}
