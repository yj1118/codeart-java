package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.repository.access.internal.SqlStatement;
import apros.codeart.util.StringUtil;

class SqlUpdateBuilder {

	private final StringBuilder _sql;
	private boolean _setFielded;
	private boolean _setWhered;

	public SqlUpdateBuilder() {
		_sql = new StringBuilder();
		_setFielded = false;
		_setWhered = false;
	}

	public void setTable(String tbName) {
		StringUtil.appendFormat(_sql, "update \"%s\" set", tbName);
	}

	public void addField(String field) {
		this.set(String.format("%s=@%s", SqlStatement.qualifier(field), field));
	}

	public void set(String setSql) {
		if (_setFielded) {
			StringUtil.appendFormat(_sql, ",%s", setSql);
			return;
		}
		StringUtil.appendFormat(_sql, " %s", setSql);
		_setFielded = true;
	}

	public void where(String... fields) {

		for (var field : fields) {
			String cmd = _setWhered ? " and " : " where ";
			_sql.append(cmd);
			StringUtil.appendFormat(_sql, "\"%s\"=@%s", field, field);
			_setWhered = true;
		}
	}

	/**
	 * 该sql语句是否能执行
	 * 
	 * @return
	 */
	public boolean canPerform() {
		return _setFielded;
	}

	public String getCommandText() {
		return _sql.toString();
	}

}
