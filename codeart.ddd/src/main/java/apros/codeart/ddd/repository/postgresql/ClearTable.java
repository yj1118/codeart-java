package apros.codeart.ddd.repository.postgresql;

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
		StringBuilder sql = new StringBuilder();
		StringUtil.appendFormat(sql, "IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = '%s') THEN", table.name());
		StringUtil.appendLine(sql);
		StringUtil.appendFormat(sql, "TRUNCATE TABLE [%s]", table.name());
		StringUtil.appendLine(sql);
		sql.append("END IF;");
		return sql.toString();
	}

	public static final ClearTable Instance = new ClearTable();

}
