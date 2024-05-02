package apros.codeart.ddd.repository.sqlserver;

import java.text.MessageFormat;

import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.GeneratedField;
import apros.codeart.ddd.repository.access.UpdateDataVersionQB;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.SafeAccess;

@SafeAccess
class UpdateDataVersion extends UpdateDataVersionQB {
	private UpdateDataVersion() {
	}

	@Override
	protected String buildImpl(DataTable table) {

		var sql = new SqlUpdateBuilder();
		sql.setTable(table.name());

		sql.set(MessageFormat.format("{0}={0}+1", SqlStatement.qualifier(GeneratedField.DataVersionName)));

		for (var field : table.primaryKeys()) {
			sql.where(field.name());
		}

		return sql.getCommandText();
	}

	public static final UpdateDataVersion Instance = new UpdateDataVersion();
}