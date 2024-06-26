package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.DropTableQB;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
class DropTable extends DropTableQB {
	private DropTable() {
	}

	@Override
	protected String buildImpl(DataTable table) {

		StringBuilder sb = new StringBuilder();
		StringUtil.appendFormat(sb,"DROP TABLE IF EXISTS \"%s\";", table.name());
		StringUtil.appendLine(sb);
		StringUtil.appendFormat(sb,"DROP TABLE IF EXISTS \"%sIncrement\";", table.name());
		StringUtil.appendLine(sb);
		StringUtil.appendFormat(sb,"DROP FUNCTION IF EXISTS get_%s_increment;", table.name());
		return  sb.toString();
	}

	public static final DropTable Instance = new DropTable();

}
