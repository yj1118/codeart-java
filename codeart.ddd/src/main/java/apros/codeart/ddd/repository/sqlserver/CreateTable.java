package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.repository.db.DBUtil;
import com.google.common.collect.Iterables;

import apros.codeart.ddd.repository.access.CreateTableQB;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.DbType;
import apros.codeart.ddd.repository.access.IDataField;
import apros.codeart.ddd.repository.access.internal.AccessUtil;
import apros.codeart.ddd.validation.TimePrecisions;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
class CreateTable extends CreateTableQB {

    private CreateTable() {
    }

    @Override
    protected String buildImpl(DataTable table) {
        StringBuilder sql = new StringBuilder();
        StringUtil.appendFormat(sql, "if ISNULL(object_id(N'[%s]'),'') = 0", table.name());
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql, "begin");
        StringUtil.appendFormat(sql, "	CREATE TABLE [%s](", table.name());
        StringUtil.appendLine(sql);

        for (var field : table.fields()) {
            StringUtil.appendLine(sql, getFieldSql(field));
        }

        StringUtil.appendFormat(sql, "	%s)", getPrimaryKeySql(table));
        StringUtil.appendLine(sql);
        StringUtil.appendValidLine(sql, getClusteredIndexSql(table));
        StringUtil.appendValidLine(sql, getNonclusteredIndexSql(table));
        sql.append("end");
        if (DBUtil.needInc(table)) {
            StringUtil.appendLine(sql);
            var code = getCreateIncCode(table.name());
            StringUtil.append(sql, code);
        }
        return sql.toString();
    }

    private static String getFieldSql(IDataField field) {
        boolean allowNull = field.tip().isEmptyable() || field.isAdditional();

        switch (field.dbType()) {
            case DbType.AnsiString:
            case DbType.String: {
                var maxLength = AccessUtil.getMaxLength(field.tip());
                var isASCII = field.dbType() == DbType.AnsiString || AccessUtil.isASCIIString(field.tip());
                var max = isASCII ? 8000 : 4000;
                return String.format("[%s] [%s](%s) %s NULL,", field.name(), (isASCII ? "varchar" : "nvarchar"),
                        ((maxLength == 0 || maxLength > max) ? "max" : maxLength),
                        (allowNull ? StringUtil.empty() : "NOT"));
            }
            case DbType.LocalDateTime: {
                var precision = AccessUtil.getTimePrecision(field.tip());
                var pv = getTimePrecisionValue(precision);
                return String.format("[%s] [%s](%s) %s NULL,", field.name(), "datetime2", pv,
                        (allowNull ? StringUtil.empty() : "NOT"));
            }
            case DbType.ZonedDateTime: {
                var precision = AccessUtil.getTimePrecision(field.tip());
                var pv = getTimePrecisionValue(precision);
                return String.format("[%s] [%s](%s) %s NULL,", field.name(), "datetimeoffset", pv,
                        (allowNull ? StringUtil.empty() : "NOT"));
            }
            default:
                return String.format("[%s] [%s] %s NULL,", field.name(), SQLServerUtil.getSqlDbTypeString(field.dbType()),
                        (allowNull ? StringUtil.empty() : "NOT"));
        }
    }

    private static int getTimePrecisionValue(TimePrecisions value) {
        switch (value) {
            case TimePrecisions.Second:
                return 0;
            case TimePrecisions.Millisecond100:
                return 1;
            case TimePrecisions.Millisecond10:
                return 2;
            case TimePrecisions.Millisecond:
                return 3;
            case TimePrecisions.Microsecond100:
                return 4;
            case TimePrecisions.Microsecond10:
                return 5;
            case TimePrecisions.Microsecond:
                return 6;
            case TimePrecisions.Nanosecond100:
                return 7;
        }
        return 0;
    }

    private static String getPrimaryKeySql(DataTable table) {
        var primaryKeys = table.primaryKeys();

        if (Iterables.size(primaryKeys) == 0)
            return StringUtil.empty();
        StringBuilder sql = new StringBuilder();
        StringUtil.appendFormat(sql, "CONSTRAINT [PK_%s", table.name());

        for (var field : primaryKeys) {
            StringUtil.appendFormat(sql, "_%s", field.name());
        }
        sql.append("] PRIMARY KEY CLUSTERED (");

        for (var field : primaryKeys) {
            StringUtil.appendFormat(sql, "[%s],", field.name());
        }
        StringUtil.removeLast(sql);
        sql.append(") ON [PRIMARY]");
        return sql.toString();
    }

    private static String getClusteredIndexSql(DataTable table) {
        var clusteredIndexs = table.clusteredIndexs();

        if (Iterables.size(clusteredIndexs) == 0)
            return StringUtil.empty();
        StringBuilder sql = new StringBuilder();
        StringUtil.appendFormat(sql, "CREATE CLUSTERED INDEX [IX_%s", table.name());

        for (var field : clusteredIndexs) {
            StringUtil.appendFormat(sql, "_%s", field.name());
        }
        StringUtil.appendFormat(sql, "] ON [%s](", table.name());

        for (var field : clusteredIndexs) {
            StringUtil.appendFormat(sql, "[%s],", field.name());
        }
        StringUtil.removeLast(sql);
        sql.append(")");
        return sql.toString();
    }

    private static String getNonclusteredIndexSql(DataTable table) {
        var nonclusteredIndexs = table.nonclusteredIndexs();

        if (Iterables.size(nonclusteredIndexs) == 0)
            return StringUtil.empty();
        StringBuilder sql = new StringBuilder();
        StringUtil.appendFormat(sql, "CREATE NONCLUSTERED INDEX [IX_%s", table.name());

        for (var field : nonclusteredIndexs) {
            StringUtil.appendFormat(sql, "_%s", field.name());
        }
        StringUtil.appendFormat(sql, "] ON [%s](", table.name());

        for (var field : nonclusteredIndexs) {
            StringUtil.appendFormat(sql, "[%s],", field.name());
        }
        StringUtil.removeLast(sql);
        sql.append(")");
        return sql.toString();
    }

    private static String getCreateIncCode(String tableName) {
        String increment = String.format("%sIncrement", tableName);

        StringBuilder sql = new StringBuilder();
        StringUtil.appendFormat(sql, "if(object_id('[%s]') is null)", increment);
        StringUtil.appendLine(sql, "begin");
        StringUtil.appendLine(sql, "	create table [" + increment + "]([value] [bigint] NOT NULL,CONSTRAINT [PK_"
                + increment
                + "] PRIMARY KEY CLUSTERED ([value] ASC)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]) ON [PRIMARY];");
        StringUtil.appendFormat(sql, "insert into [%s]([value]) values(0);", increment);
        StringUtil.appendLine(sql, "end");
        return sql.toString();
    }

    public static final CreateTable Instance = new CreateTable();

}
