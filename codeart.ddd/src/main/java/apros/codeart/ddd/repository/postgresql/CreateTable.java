package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.repository.access.CreateTableQB;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.DbType;
import apros.codeart.ddd.repository.access.IDataField;
import apros.codeart.ddd.repository.access.internal.AccessUtil;
import apros.codeart.ddd.repository.postgresql.Util;
import apros.codeart.ddd.validation.TimePrecisions;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;
import com.google.common.collect.Iterables;

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

		for (var field : table.fields()) {
			StringUtil.appendLine(sql, getFieldSql(field));
		}

		StringUtil.appendFormat(sql, "%s", getPrimaryKeySql(table));
		StringUtil.appendLine(sql);
		StringUtil.appendValidLine(sql, getClusteredIndexSql(table));
		StringUtil.appendValidLine(sql, getNonclusteredIndexSql(table));
		sql.append(");");
		return sql.toString();
	}

	private static String getFieldSql(IDataField field) {
		boolean allowNull = field.tip().isEmptyable() || field.isAdditional();

		switch (field.dbType()) {
		case DbType.AnsiString:
		case DbType.String: {
			var maxLength = AccessUtil.getMaxLength(field.tip());

			if(maxLength <=0){
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

		return getPrimaryKeySql(table,primaryKeys);
	}

	private static String getPrimaryKeySql(DataTable table,Iterable<IDataField> fields) {

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
		return getPrimaryKeySql(table,clusteredIndexs);
	}

	private static String getNonclusteredIndexSql(DataTable table) {
		var nonclusteredIndexs = table.nonclusteredIndexs();

		if (Iterables.size(nonclusteredIndexs) == 0)
			return StringUtil.empty();
		StringBuilder sql = new StringBuilder();
		StringUtil.appendFormat(sql, "CREATE INDEX IX_%s", table.name());

		for (var field : nonclusteredIndexs) {
			StringUtil.appendFormat(sql, "_%s", field.name());
		}
		StringUtil.appendFormat(sql, " ON %s(", table.name());

		for (var field : nonclusteredIndexs) {
			StringUtil.appendFormat(sql, "\"%s\",", field.name());
		}
		StringUtil.removeLast(sql);
		sql.append(");");
		return sql.toString();
	}

	public static final CreateTable Instance = new CreateTable();

}
