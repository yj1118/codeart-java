package apros.codeart.ddd.repository.db;

import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.GeneratedField;
import apros.codeart.ddd.repository.access.GetSlaveIdsQB;
import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
public class GetSlaveIds extends GetSlaveIdsQB {
	private GetSlaveIds() {
	}

	@Override
	protected String buildImpl(DataTable table) {
		DataTable slave = table;
		DataTable master = slave.master();
		DataTable root = slave.root();
		DataTable middle = slave.middle();

		var rootId = GeneratedField.RootIdName;
		var slaveId = GeneratedField.SlaveIdName;

		StringBuilder sql = new StringBuilder();
		if (root.same(master)) {
			StringUtil.appendMessageFormat(sql, "select {1} from {2} where {2}.{0} = @{0} order by {3} asc",
					SqlStatement.qualifier(rootId),
					SqlStatement.qualifier(slaveId),
					SqlStatement.qualifier(middle.name()),
					SqlStatement.qualifier(GeneratedField.OrderIndexName));
		} else {
			var masterId = GeneratedField.MasterIdName;

			StringUtil.appendMessageFormat(sql,
					"select {1} from {2} where {2}.{0} = @{0} and {2}.{3}=@{3} order by {4} asc",
					SqlStatement.qualifier(rootId),
					SqlStatement.qualifier(slaveId),
					SqlStatement.qualifier(middle.name()),
					SqlStatement.qualifier(masterId),
					SqlStatement.qualifier(GeneratedField.OrderIndexName));
		}
		return sql.toString();
	}

	public static final GetSlaveIds Instance = new GetSlaveIds();

}
