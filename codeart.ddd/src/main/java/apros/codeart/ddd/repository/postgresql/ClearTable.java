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
		String increment = String.format("%sIncrement", table.name());

		StringBuilder sql = new StringBuilder();
		StringUtil.appendLine(sql, "DO $$");
		StringUtil.appendLine(sql, "BEGIN");
		StringUtil.appendLine(sql);
		StringUtil.appendFormat(sql, "IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = '%s') THEN", table.name());
		StringUtil.appendLine(sql);
		StringUtil.appendFormat(sql, "TRUNCATE TABLE \"%s\";", table.name());
		StringUtil.appendLine(sql);
		StringUtil.appendMessageFormat(sql, "UPDATE \"{0}\" SET \"value\"=0;", increment);
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, "END IF;");
		StringUtil.append(sql, "END $$;");
		return sql.toString();
	}

	public static final ClearTable Instance = new ClearTable();

}
