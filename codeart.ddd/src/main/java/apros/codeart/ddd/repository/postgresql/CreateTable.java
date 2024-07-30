package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.repository.access.*;
import apros.codeart.ddd.repository.access.internal.AccessUtil;
import apros.codeart.ddd.repository.db.DBUtil;
import apros.codeart.ddd.repository.postgresql.Util;
import apros.codeart.ddd.validation.TimePrecisions;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

import java.util.function.Function;

@SafeAccess
class CreateTable extends CreateTableQB {

    private CreateTable() {
    }

    @Override
    protected String buildImpl(DataTable table) {

        StringBuilder sql = new StringBuilder();
        StringUtil.appendFormat(sql, "CREATE TABLE IF NOT EXISTS \"%s\"", table.name());
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql, "(");

        var pkSql = getPrimaryKeySql(table);

        var fieldCount = Iterables.size(table.fields());
        var index = 0;
        for (var field : table.fields()) {
            StringUtil.append(sql, getFieldSql(field));
            index++;
            if (index == fieldCount && StringUtil.isNullOrEmpty(pkSql))
                StringUtil.removeLast(sql);
            StringUtil.appendLine(sql);
        }

        StringUtil.appendFormat(sql, "%s", pkSql);
        StringUtil.appendLine(sql);
        sql.append(");");
        StringUtil.appendLine(sql);
        StringUtil.appendValidLine(sql, getClusteredIndexSql(table));
        StringUtil.appendValidLine(sql, getNonclusteredIndexSql(table));

        if (DBUtil.needInc(table)) {
            StringUtil.appendLine(sql);
            var code = getFUNCTIONCode(table.name());
            StringUtil.append(sql, code);
        }

        if (table.type() == DataTableType.Middle) {
            var code = DeleteTable.getPROCEDURE_DeleteMiddle_Code(table);
            if (!StringUtil.isNullOrEmpty(code)) {
                StringUtil.appendLine(sql);
                StringUtil.append(sql, code);
            }
        }

        return sql.toString();
    }

    private static String getFieldSql(IDataField field) {
        boolean allowNull = field.tip().isEmptyable() || field.isAdditional();

        switch (field.dbType()) {
            case DbType.AnsiString:
            case DbType.String: {
                var maxLength = AccessUtil.getMaxLength(field.tip());

                if (maxLength <= 0) {
                    return String.format("\"%s\" text %s,", field.name(),
                            (allowNull ? StringUtil.empty() : "NOT NULL"));
                }

                return String.format("\"%s\" varchar(%s) %s,", field.name(),
                        maxLength,
                        (allowNull ? StringUtil.empty() : "NOT NULL"));
            }
            case DbType.LocalDateTime: {
                var precision = AccessUtil.getTimePrecision(field.tip());
                var pv = getTimePrecisionValue(precision);
                return String.format("\"%s\" %s(%s) %s,", field.name(), "timestamp", pv,
                        (allowNull ? StringUtil.empty() : "NOT NULL"));
            }
            case DbType.ZonedDateTime: {
                var precision = AccessUtil.getTimePrecision(field.tip());
                var pv = getTimePrecisionValue(precision);
                return String.format("\"%s\" %s(%s) %s,", field.name(), "timestamptz", pv,
                        (allowNull ? StringUtil.empty() : "NOT NULL"));
            }
            default:
                return String.format("\"%s\" %s %s,", field.name(), Util.getSqlDbTypeString(field.dbType()),
                        (allowNull ? StringUtil.empty() : "NOT NULL"));
        }
    }

    private static int getTimePrecisionValue(TimePrecisions value) {
        return switch (value) {
            case TimePrecisions.Second -> 0;
            case TimePrecisions.Millisecond100 -> 1;
            case TimePrecisions.Millisecond10 -> 2;
            case TimePrecisions.Millisecond -> 3;
            case TimePrecisions.Microsecond100 -> 4;
            case TimePrecisions.Microsecond10 -> 5;
            case TimePrecisions.Microsecond -> 6;
            case TimePrecisions.Nanosecond100 -> 7;
        };
    }

    private static String getPrimaryKeySql(DataTable table) {
        var primaryKeys = table.primaryKeys();

        return getPrimaryKeySql(table, primaryKeys);
    }

    private static String getPrimaryKeySql(DataTable table, Iterable<IDataField> fields) {

        if (Iterables.size(fields) == 0)
            return StringUtil.empty();
        StringBuilder sql = new StringBuilder();
        StringUtil.appendFormat(sql, "CONSTRAINT PK_%s", table.name());

        for (var field : fields) {
            StringUtil.appendFormat(sql, "_%s", field.name());
        }
        sql.append(" PRIMARY KEY (");

        for (var field : fields) {
            StringUtil.appendFormat(sql, "\"%s\",", field.name());
        }
        StringUtil.removeLast(sql);
        sql.append(")");
        return sql.toString();
    }

    private static String getClusteredIndexSql(DataTable table) {
        var clusteredIndexs = table.clusteredIndexs();

        // postgresql里的聚集索引就是多字段主键
        return getPrimaryKeySql(table, clusteredIndexs);
    }

    private static String getNonclusteredIndexSql(DataTable table) {
        var nonclusteredIndexs = table.nonclusteredIndexs();

        if (Iterables.size(nonclusteredIndexs) == 0)
            return StringUtil.empty();
        StringBuilder sql = new StringBuilder();
        StringUtil.appendFormat(sql, "CREATE INDEX IF NOT EXISTS IX_%s", table.name());

        for (var field : nonclusteredIndexs) {
            StringUtil.appendFormat(sql, "_%s", field.name());
        }
        StringUtil.appendFormat(sql, " ON \"%s\"(", table.name());

        for (var field : nonclusteredIndexs) {
            StringUtil.appendFormat(sql, "\"%s\",", field.name());
        }
        StringUtil.removeLast(sql);
        sql.append(");");
        return sql.toString();
    }


    private static String getFUNCTIONCode(String tableName) {
        String increment = String.format("%sIncrement", tableName);

        StringBuilder sql = new StringBuilder();
        StringUtil.appendLine(sql, "DO $$");
        StringUtil.appendLine(sql, "BEGIN");
        StringUtil.appendFormat(sql, "IF NOT EXISTS (SELECT FROM pg_catalog.pg_tables WHERE tablename = '%s') THEN", increment);
        StringUtil.appendLine(sql);
        StringUtil.appendFormat(sql, "CREATE TABLE \"%s\"", increment);
        StringUtil.appendLine(sql, "(");
        StringUtil.appendLine(sql, "    \"value\" bigint NOT NULL,CONSTRAINT PK_"
                + increment
                + " PRIMARY KEY (\"value\")");
        StringUtil.appendLine(sql, ");");
        StringUtil.appendFormat(sql, "INSERT INTO \"%s\"(\"value\") VALUES(0);", increment);
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql, "END IF;");
        StringUtil.appendLine(sql, "END $$;");
        StringUtil.appendFormat(sql, "CREATE OR REPLACE FUNCTION get_%s_increment()", tableName);
        StringUtil.appendLine(sql);
        StringUtil.appendLine(sql, "RETURNS TABLE(value bigint)");
        StringUtil.appendLine(sql, "LANGUAGE plpgsql");
        StringUtil.appendLine(sql, "AS $$");
        StringUtil.appendLine(sql, "BEGIN");
        StringUtil.appendMessageFormat(sql, "UPDATE \"{0}\" SET \"value\"=\"{0}\".\"value\"+1;", increment);
        StringUtil.appendLine(sql);
        StringUtil.appendMessageFormat(sql, "RETURN QUERY SELECT \"{0}\".\"value\" FROM  \"{0}\";", increment);
        StringUtil.appendLine(sql);
        StringUtil.appendFormat(sql, "END $$;");
        return sql.toString();
    }

    public static final CreateTable Instance = new CreateTable();

}
