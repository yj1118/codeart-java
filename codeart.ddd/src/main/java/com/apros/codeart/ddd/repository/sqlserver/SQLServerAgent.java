package com.apros.codeart.ddd.repository.sqlserver;

import com.apros.codeart.ddd.QueryLevel;
import com.apros.codeart.ddd.repository.access.CreateTableQB;
import com.apros.codeart.ddd.repository.access.DatabaseAgent;
import com.apros.codeart.ddd.repository.access.DropTableQB;
import com.apros.codeart.ddd.repository.access.IDatabaseAgent;
import com.apros.codeart.ddd.repository.access.InsertTableQB;
import com.apros.codeart.ddd.repository.access.QueryObjectQB;
import com.apros.codeart.util.StringUtil;

public class SQLServerAgent extends DatabaseAgent {

	private SQLServerAgent() {
		// 注入默认的支持
		this.registerQueryBuilder(CreateTableQB.class, CreateTable.Instance);
		this.registerQueryBuilder(DropTableQB.class, DropTable.Instance);
		this.registerQueryBuilder(InsertTableQB.class, InsertTable.Instance);
		this.registerQueryBuilder(QueryObjectQB.class, QueryObject.Instance);
	}

	@Override
	public String supplementLock(String sql, QueryLevel level) {
		return LockSql.get(sql, level);
	}

	public static final IDatabaseAgent Instance = new SQLServerAgent();

	@Override
	public String getIncrIdSql(String tableName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStringIndexableMaxLength() {
		// TODO Auto-generated method stub
		return 0;
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
