package apros.codeart.ddd.repository.sqlserver;

import apros.codeart.util.StringUtil;

class SqlInsertBuilder {
	private StringBuilder _names = new StringBuilder();
	private StringBuilder _paras = new StringBuilder();
	private String _tbName;

	public SqlInsertBuilder(String tbName) {
		_tbName = tbName;
	}

	public void addField(String field) {
		StringUtil.appendFormat(_names, "[{0}],", field);
		StringUtil.appendFormat(_paras, "@{0},", field);
	}

	public String getCommandText() {
		String sql = StringUtil.empty();
		if (_names.length() > 0) {
			StringUtil.removeLast(_names);
			StringUtil.removeLast(_paras);
			sql = String.format("insert into [%s](%s) values(%s)", _tbName, _names.toString(), _paras.toString());
			// 还原状态
			_names.append(",");
			_paras.append(",");
		}
		return sql;
	}
}
