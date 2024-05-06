package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.ddd.repository.access.ClearTableQB;
import apros.codeart.ddd.repository.access.CreateTableQB;
import apros.codeart.ddd.repository.access.DatabaseAgent;
import apros.codeart.ddd.repository.access.DecrementAssociatedQB;
import apros.codeart.ddd.repository.access.DeleteTableQB;
import apros.codeart.ddd.repository.access.DropTableQB;
import apros.codeart.ddd.repository.access.GetAssociatedQB;
import apros.codeart.ddd.repository.access.GetPrimitiveValuesQB;
import apros.codeart.ddd.repository.access.GetSlaveIdsQB;
import apros.codeart.ddd.repository.access.IDatabaseAgent;
import apros.codeart.ddd.repository.access.IncrementAssociatedQB;
import apros.codeart.ddd.repository.access.InsertTableQB;
import apros.codeart.ddd.repository.access.QueryObjectQB;
import apros.codeart.ddd.repository.access.QueryPageQB;
import apros.codeart.ddd.repository.access.UpdateTableQB;
import apros.codeart.util.StringUtil;

public class SQLServerAgent extends DatabaseAgent {

	private SQLServerAgent() {
		// 注入默认的支持
		this.registerQueryBuilder(CreateTableQB.class, CreateTable.Instance);
		this.registerQueryBuilder(DropTableQB.class, DropTable.Instance);
		this.registerQueryBuilder(InsertTableQB.class, InsertTable.Instance);
		this.registerQueryBuilder(DeleteTableQB.class, DeleteTable.Instance);
		this.registerQueryBuilder(ClearTableQB.class, ClearTable.Instance);
		this.registerQueryBuilder(UpdateTableQB.class, UpdateTable.Instance);

		this.registerQueryBuilder(QueryObjectQB.class, QueryObject.Instance);

		this.registerQueryBuilder(IncrementAssociatedQB.class, IncrementAssociated.Instance);
		this.registerQueryBuilder(DecrementAssociatedQB.class, DecrementAssociated.Instance);
		this.registerQueryBuilder(GetAssociatedQB.class, GetAssociated.Instance);

		this.registerQueryBuilder(GetPrimitiveValuesQB.class, GetPrimitiveValues.Instance);
		this.registerQueryBuilder(GetSlaveIdsQB.class, GetSlaveIds.Instance);
		this.registerQueryBuilder(QueryPageQB.class, QueryPage.Instance);
	}

	public static final IDatabaseAgent Instance = new SQLServerAgent();

	@Override
	public String getIncrIdSql(String tableName) {
		String increment = String.format("%sIncrement", tableName);

		StringBuilder sql = new StringBuilder();
		StringUtil.appendLine(sql, "begin transaction;");
		StringUtil.appendFormat(sql, "if(object_id('[%s]') is null)", increment);
		StringUtil.appendLine(sql, "begin");
		StringUtil.appendLine(sql, "	create table [" + increment + "]([value] [bigint] NOT NULL,CONSTRAINT [PK_"
				+ increment
				+ "] PRIMARY KEY CLUSTERED ([value] ASC)WITH (IGNORE_DUP_KEY = OFF) ON [PRIMARY]) ON [PRIMARY];");

		StringUtil.appendLine(sql, "end");
		StringUtil.appendFormat(sql, "if(not exists(select 1 from [%s] with(xlock,holdlock)))", increment);
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, "begin");

		StringUtil.appendFormat(sql, " insert into [{0}](value) values(1);", increment);
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, " select cast(1 as bigint) as value;");
		StringUtil.appendLine(sql, "end");

		StringUtil.appendLine(sql, "else");
		StringUtil.appendLine(sql, "begin");

		StringUtil.appendFormat(sql, " update [{0}] set [value]=[value]+1;", increment);
		StringUtil.appendLine(sql);

		StringUtil.appendFormat(sql, "select value from [{0}] with(nolock);", increment);
		StringUtil.appendLine(sql);

		StringUtil.appendLine(sql, "end");
		StringUtil.appendLine(sql, "commit;");
		return sql.toString();
	}

	/**
	 * 可被索引得字符串得最大长度
	 */
	@Override
	public int getStringIndexableMaxLength() {
		return 400;
	}

	@Override
	public String qualifier(String field) {
		if (!field.startsWith("["))
			return String.format("[%s]", field);
		return field;
	}

	@Override
	public String unQualifier(String field) {
		if (field.startsWith("["))
			return StringUtil.substr(field, 1, (field.length() - 2));
		return field;
	}
}
