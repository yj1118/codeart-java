package com.apros.codeart.ddd.repository.sqlserver;

import com.apros.codeart.ddd.EntityObject;
import com.apros.codeart.ddd.repository.access.DataTableType;
import com.apros.codeart.ddd.repository.access.GeneratedField;
import com.apros.codeart.ddd.repository.access.QueryDescription;
import com.apros.codeart.ddd.repository.access.UpdateTableQB;
import com.apros.codeart.util.SafeAccess;

@SafeAccess
class UpdateTable extends UpdateTableQB {
	private UpdateTable() {
	}

	@Override
	public String build(QueryDescription description) {
		var table = description.table();
		var param = description.param();

		var sql = new SqlUpdateBuilder();
		sql.setTable(table.name());

		for (var p : param) {
			var field = p.getKey();
			if (field.equalsIgnoreCase(EntityObject.IdPropertyName))
				continue; // 不修改id和rooid
			if (table.type() != DataTableType.AggregateRoot) {
				if (field.equalsIgnoreCase(GeneratedField.RootIdName))
					continue; // 不修改rooid
			}
			sql.addField(field);
		}

		for (var field : table.primaryKeys()) {
			sql.where(field.name());
		}

		return sql.getCommandText();
	}

	public static final UpdateTable Instance = new UpdateTable();
}
