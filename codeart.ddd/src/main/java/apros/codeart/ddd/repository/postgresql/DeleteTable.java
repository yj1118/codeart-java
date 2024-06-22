package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.repository.access.*;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

import java.text.MessageFormat;
import java.util.ArrayList;

@SafeAccess
public class DeleteTable extends DeleteTableQB {
	private DeleteTable() {
	}

	@Override
	protected String buildImpl(DataTable table) {
		switch (table.type()) {
		case DataTableType.AggregateRoot: {
			return getDeleteRootSql(table);
		}
		case DataTableType.ValueObject:
		case DataTableType.EntityObject: {
			return getDeleteMemberSql(table);
		}
		case DataTableType.Middle: {
			return getDeleteMiddleSql(table);
		}
		}
		return StringUtil.empty();
	}

	private static String getDeleteRootSql(DataTable table) {
		ArrayList<String> sqls = new ArrayList<String>();

		var index = new TempDataTableIndex();

		fillDeleteByRootIdSql(table, sqls, index);

		StringBuilder code = new StringBuilder();
		for (var sql : sqls) {
			StringUtil.appendLine(code, sql);
		}
		return code.toString().trim();
	}

	private static void fillDeleteByRootIdSql(DataTable table, ArrayList<String> sqls, TempDataTableIndex index) {
		for (var child : table.buildtimeChilds()) {
			if (!index.tryAdd(child))
				continue; // 已处理过

			if (child.type() == DataTableType.AggregateRoot)
				continue;// 不删除引用的外部根表
			fillDeleteByRootIdSql(child, sqls, index);

			if (child.middle() != null)
				fillDeleteByRootIdSql(child.middle(), sqls, index);
		}

		var selfSql = getDeleteByRootIdSql(table);
		if (!sqls.contains(selfSql))
			sqls.add(selfSql);
	}

	private static String getDeleteByRootIdSql(DataTable table) {
		if (table.type() == DataTableType.AggregateRoot) {
			return MessageFormat.format("delete \"{0}\" where \"{1}\"=@{1};", table.name(), EntityObject.IdPropertyName);
		} else {
			return MessageFormat.format("delete \"{0}\" where \"{1}\"=@{1};", table.name(), GeneratedField.RootIdName);
		}
	}

	private static String getDeleteMemberSql(DataTable table) {
		return MessageFormat.format("delete \"{0}\" where \"{1}\"=@{1} and \"{2}\"=@{2};", table.name(),
				GeneratedField.RootIdName, EntityObject.IdPropertyName);
	}

	/**
	 * 删除中间表数据（根据参数判断由什么条件删除）
	 * 
	 * @param middle
	 * @return
	 */
	private static String getDeleteMiddleSql(DataTable middle) {
		if (middle.isPrimitiveValue()) {
			return getDeleteMiddleByPrimitiveValuesSql(middle);
		}

		var rootId = GeneratedField.RootIdName;
		var slaveId = GeneratedField.SlaveIdName;

		if (middle.root().equals(middle.master())) {
			StringBuilder sql = new StringBuilder();
			StringUtil.appendFormat(sql, "IF @%s IS NULL THEN", rootId); // 根据slave删除
			StringUtil.appendLine(sql);

			StringUtil.appendMessageFormat(sql, "delete \"{0}\" where \"{1}\"=@{1};", middle.name(), slaveId);

			StringUtil.appendLine(sql);

			StringUtil.appendLine(sql, "RETURN;");
			StringUtil.appendLine(sql, "END IF;");

			StringUtil.appendFormat(sql, "IF @%s IS NULL THEN", slaveId);
			StringUtil.appendLine(sql);
			StringUtil.appendMessageFormat(sql, "delete \"{0}\" where \"{1}\"=@{1};", middle.name(), rootId);

			StringUtil.appendLine(sql);
			StringUtil.appendLine(sql, "ELSE");

			StringUtil.appendMessageFormat(sql, "delete \"{0}\" where \"{1}\"=@{1} and \"{2}\"=@{2};", middle.name(), rootId,
					slaveId);

			StringUtil.appendLine(sql, "END IF;");


			return sql.toString();
		} else {
			var masterId = GeneratedField.MasterIdName;

			StringBuilder sql = new StringBuilder();
			StringUtil.appendFormat(sql, "IF @%s IS NULL THEN", rootId); // 根据slave删除
			StringUtil.appendLine(sql);

			StringUtil.appendMessageFormat(sql, "delete \"{0}\" where \"{1}\"=@{1};", middle.name(), slaveId);

			StringUtil.appendLine(sql);
			StringUtil.appendLine(sql, "RETURN;");
			StringUtil.appendLine(sql, "END IF;");

			StringUtil.appendFormat(sql, "IF @%s IS NULL THEN", slaveId);

			StringUtil.appendLine(sql);
			StringUtil.appendMessageFormat(sql, "delete \"{0}\" where \"{1}\"=@{1} and \"{2}\"=@{2};", middle.name(), rootId,
					masterId);

			StringUtil.appendLine(sql);
			StringUtil.appendLine(sql, "ELSE");

			StringUtil.appendMessageFormat(sql, "delete \"{0}\" where \"{1}\"=@{1} and \"{2}\"=@{2};", middle.name(), rootId,
					slaveId);


			StringUtil.appendLine(sql, "END IF;");

			return sql.toString();

		}
	}

	private static String getDeleteMiddleByPrimitiveValuesSql(DataTable middle) {
		var rootId = GeneratedField.RootIdName;

		if (middle.root().equals(middle.master())) {
			return MessageFormat.format("delete \"{0}\" where \"{1}\"=@{1};", middle.name(), rootId);
		} else {
			var masterId = GeneratedField.MasterIdName;

			return MessageFormat.format("delete \"{0}\" where \"{1}\"=@{1} and \"{2}\"=@{2};", middle.name(), rootId,
					masterId);
		}
	}

	public static final DeleteTable Instance = new DeleteTable();
}
