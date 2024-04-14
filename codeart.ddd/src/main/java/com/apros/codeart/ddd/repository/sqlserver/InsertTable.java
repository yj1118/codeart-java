package com.apros.codeart.ddd.repository.sqlserver;

import com.apros.codeart.ddd.repository.access.DataTable;
import com.apros.codeart.ddd.repository.access.InsertTableQB;
import com.apros.codeart.util.SafeAccess;

@SafeAccess
class InsertTable extends InsertTableQB {
	private InsertTable() {
	}

	@Override
	protected String buildImpl(DataTable table) {
		SqlInsertBuilder sql = new SqlInsertBuilder(table.name());
		for (var field : table.fields()) {
			if (field.isAdditional())
				continue; // 附加字段由数据映射器维护
			sql.addField(field.name());
		}

		return sql.getCommandText();
	}

	public static final InsertTable Instance = new InsertTable();
}
