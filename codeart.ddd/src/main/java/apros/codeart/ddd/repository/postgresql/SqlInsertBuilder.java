package apros.codeart.ddd.repository.postgresql;

import apros.codeart.util.StringUtil;

class SqlInsertBuilder {
    private final StringBuilder _names = new StringBuilder();
    private final StringBuilder _paras = new StringBuilder();
    private final String _tbName;

    public SqlInsertBuilder(String tbName) {
        _tbName = tbName;
    }

    public void addField(String field) {
        StringUtil.appendFormat(_names, "\"%s\",", field);
        StringUtil.appendFormat(_paras, "@%s,", field);
    }

    public String getCommandText() {
        String sql = StringUtil.empty();
        if (!_names.isEmpty()) {
            StringUtil.removeLast(_names);
            StringUtil.removeLast(_paras);
            sql = String.format("insert into \"%s\"(%s) values(%s)", _tbName, _names.toString(), _paras.toString());
            // 还原状态
            _names.append(",");
            _paras.append(",");
        }
        return sql;
    }
}
