package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.repository.access.ClearTableQB;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
class ClearTable extends ClearTableQB {
	private ClearTable() {
	}

	@Override
	protected String buildImpl(DataTable table) {
		String increment = String.format("%sIncrement", table.name());

		StringBuilder sql = new StringBuilder();
		StringUtil.appendFormat(sql, "if ISNULL(object_id(N'[%s]'),'') > 0", table.name());
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, "begin");
		StringUtil.appendFormat(sql, "TRUNCATE TABLE [%s]", table.name());
		StringUtil.appendLine(sql);
		StringUtil.appendFormat(sql,"end");
		StringUtil.appendLine(sql);
		StringUtil.appendFormat(sql, "if ISNULL(object_id(N'[%s]'),'') > 0", increment);
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, "begin");
		StringUtil.appendMessageFormat(sql, "UPDATE \"{0}\" SET \"value\"=0;", increment);
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql);
		sql.append("end");

		return sql.toString();
	}

	public static final ClearTable Instance = new ClearTable();

}
