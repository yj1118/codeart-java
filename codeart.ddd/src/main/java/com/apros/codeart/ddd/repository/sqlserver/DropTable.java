package com.apros.codeart.ddd.repository.sqlserver;

import com.apros.codeart.ddd.repository.access.DataTable;
import com.apros.codeart.ddd.repository.access.DropTableQB;
import com.apros.codeart.util.SafeAccess;
import com.apros.codeart.util.StringUtil;

@SafeAccess
class DropTable extends DropTableQB {
	private DropTable() {
	}

	@Override
	protected String buildImpl(DataTable table) {
		StringBuilder sql = new StringBuilder();

		StringUtil.appendFormat(sql, "if ISNULL(object_id(N'[%s]'),'') > 0", table.name());
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, "begin");
		StringUtil.appendFormat(sql, "DROP TABLE [%s]", table.name());
		StringUtil.appendLine(sql);
		sql.append("end");
		return sql.toString();
	}

	public static final DropTable Instance = new DropTable();

}
