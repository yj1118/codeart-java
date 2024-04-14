package com.apros.codeart.ddd.repository.sqlserver;

import com.apros.codeart.ddd.repository.access.ClearTableQB;
import com.apros.codeart.ddd.repository.access.DataTable;
import com.apros.codeart.util.SafeAccess;
import com.apros.codeart.util.StringUtil;

@SafeAccess
class ClearTable extends ClearTableQB {
	private ClearTable() {
	}

	@Override
	protected String buildImpl(DataTable table) {
		StringBuilder sql = new StringBuilder();
		StringUtil.appendFormat(sql, "if ISNULL(object_id(N'[%s]'),'') > 0", table.name());
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, "begin");
		StringUtil.appendFormat(sql, "TRUNCATE TABLE [%s]", table.name());
		StringUtil.appendLine(sql);
		sql.append("end");
		return sql.toString();
	}

	public static final ClearTable Instance = new ClearTable();

}
