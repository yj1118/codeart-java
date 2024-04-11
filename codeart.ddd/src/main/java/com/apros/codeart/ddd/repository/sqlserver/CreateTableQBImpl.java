package com.apros.codeart.ddd.repository.sqlserver;

import com.apros.codeart.ddd.repository.access.AccessUtil;
import com.apros.codeart.ddd.repository.access.CreateTableQB;
import com.apros.codeart.ddd.repository.access.DataTable;
import com.apros.codeart.ddd.repository.access.DbType;
import com.apros.codeart.ddd.repository.access.IDataField;
import com.apros.codeart.util.SafeAccess;
import com.apros.codeart.util.StringUtil;

@SafeAccess
class CreateTableQBImpl extends CreateTableQB {

	private CreateTableQBImpl() {
	}

	@Override
	protected String buildImpl(DataTable table) {
		StringBuilder sql = new StringBuilder();
		StringUtil.appendFormat(sql, "if ISNULL(object_id(N'[%s]'),'') = 0", table.name());
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, "begin");
		StringUtil.appendFormat(sql, "	CREATE TABLE [{0}](", table.name());
		StringUtil.appendLine(sql);

		for (var field : table.fields()) {
			StringUtil.appendLine(sql, getFieldSql(field));
		}

		StringUtil.appendFormat(sql, "	{0})", getPrimaryKeySql(table));
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, getClusteredIndexSql(table));
		StringUtil.appendLine(sql, getNonclusteredIndexSql(table));
		StringUtil.append("end");
		return sql.toString();
	}

	private static String getFieldSql(IDataField field) {
		boolean allowNull = field.tip().isEmptyable() || field.isAdditional();

		if (field.dbType() == DbType.String || field.dbType() == DbType.AnsiString) {
			var maxLength = AccessUtil.getMaxLength(field.tip());
			var isASCII = field.dbType() == DbType.AnsiString || AccessUtil.isASCIIString(field.tip());
			var max = isASCII ? 8000 : 4000;
			return String.format("[%s] [%s](%s) %s NULL,", field.name(), (isASCII ? "varchar" : "nvarchar"),
					((maxLength == 0 || maxLength > max) ? "max" : maxLength),
					(allowNull ? StringUtil.empty() : "NOT"));
		} else {
			return String.format("[%s] [%s] %s NULL,", field.name(), Util.getSqlDbTypeString(field.dbType()),
					(allowNull ? StringUtil.empty() : "NOT"));
		}
	}

	public static final CreateTableQBImpl Instance = new CreateTableQBImpl();

}
