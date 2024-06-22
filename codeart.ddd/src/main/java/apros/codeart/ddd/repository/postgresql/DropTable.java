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
		return String.format("DROP TABLE IF EXISTS %s;",table.name());
	}

	public static final DropTable Instance = new DropTable();

}
