package apros.codeart.ddd.repository.access;

import java.util.Objects;

public class QueryPageCode {
    private final String _selectSql;
    private final String _tableSql;
    private final String _orderSql;

    private DataTable _table;

    public QueryPageCode(String selectSql, String tableSql, String orderSql) {
        this._selectSql = selectSql;
        this._tableSql = tableSql;   //用于过滤重复行
        this._orderSql = orderSql;
    }

    /**
     * 绑定一个表信息后，编译器会自动格式化
     *
     * @param table
     */
    public void bind(DataTable table) {
        this._table = table;
    }

    public String selectSql() {
        return _selectSql;
    }

    public String tableSql() {
        return _tableSql;
    }

    public String orderSql() {
        return _orderSql;
    }

    public DataTable table() {
        return _table;
    }


    @Override
    public int hashCode() {
        return Objects.hash(_selectSql, _tableSql, _orderSql);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryPageCode that = (QueryPageCode) o;
        return Objects.equals(_selectSql, that._selectSql) &&
                Objects.equals(_tableSql, that._tableSql) &&
                Objects.equals(_orderSql, that._orderSql);
    }

    @Override
    public String toString() {
        return "QueryPageCode{" +
                "selectSql='" + _selectSql + '\'' +
                ", tableSql='" + _tableSql + '\'' +
                ", orderSql='" + _orderSql + '\'' +
                '}';
    }
}
