package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.GeneratedField;
import apros.codeart.ddd.repository.access.GetPrimitiveValuesQB;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

@SafeAccess
class GetPrimitiveValues extends GetPrimitiveValuesQB {
	private GetPrimitiveValues() {
	}

	@Override
	protected String buildImpl(DataTable table) {
		DataTable middle = table;
		DataTable master = middle.master();
		DataTable root = middle.root();

		var rootId = GeneratedField.RootIdName;

		StringBuilder sql = new StringBuilder();
		if (root.same(master)) {
			StringUtil.appendMessageFormat(sql, "select [{1}] from [{2}] where [{2}].[{0}] = @{0} order by [{3}] asc",
					rootId, GeneratedField.PrimitiveValueName, middle.name(), GeneratedField.OrderIndexName);
		} else {
			var masterId = GeneratedField.MasterIdName;

			StringUtil.appendMessageFormat(sql,
					"select [{1}] from {2} where [{2}].[{0}] = @{0} and [{2}].[{3}]=@{3} order by [{4}] asc", rootId,
					GeneratedField.PrimitiveValueName, middle.name(), masterId, GeneratedField.OrderIndexName);
		}
		return sql.toString();
	}

	public static final GetPrimitiveValues Instance = new GetPrimitiveValues();

}