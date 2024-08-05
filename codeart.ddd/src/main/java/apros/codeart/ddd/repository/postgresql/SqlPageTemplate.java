package apros.codeart.ddd.repository.postgresql;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.ddd.repository.db.DBUtil;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

import java.text.MessageFormat;

public class SqlPageTemplate {

    private final DataTable _table;

    private String _firstPageCode;

    private String _otherPageCode;

    public SqlPageTemplate(DataTable table) {
        _table = table;
    }

    public void build(String objectSql) {
        _firstPageCode = this.getFirstPageCode(objectSql);
        _otherPageCode = this.getOtherPageCode(objectSql);
    }

    private String getFirstPageCode(String objectSql) {
        var bottomSql = getFirstPageCT();
        bottomSql = bottomSql.replaceAll("@data_length", "{0}");// 替换成格式化参数
        return String.format("%s%s%s", objectSql, System.lineSeparator(), bottomSql);
    }

    private String getOtherPageCode(String objectSql) {

        var bottomSql = getPageCT();
        bottomSql = bottomSql.replaceAll("@data_start", "{0}");// 替换成格式化参数
        bottomSql = bottomSql.replaceAll("@data_length", "{1}");// 替换成格式化参数
        bottomSql = bottomSql.replaceAll("@data_end", "{2}");// 替换成格式化参数

        return String.format("%s%s%s", objectSql, System.lineSeparator(), bottomSql);
    }

    private String _select = StringUtil.empty();

    public void select(String format, Object... args) {
        _select = StringUtil.format(format, args);
    }

    private String _from = StringUtil.empty();

    public void from(String format, Object... args) {
        _from = StringUtil.format(format, args);
    }

    private final StringBuilder _condition = new StringBuilder();

    public void where(String format, Object... args) {
        String cmd = !_condition.isEmpty() ? " and " : " where ";
        _condition.append(cmd);
        StringUtil.appendMessageFormat(_condition, format, args);
    }

    private String _orderBy = StringUtil.empty();

    public void orderBy(SqlDefinition definition) {
        _orderBy = definition.order();
        if (StringUtil.isNullOrEmpty(_orderBy)) {
            _orderBy = String.format("order by %s asc", EntityObject.IdPropertyName);
        } else {
            var order = definition.columns().order();
            // 对于翻页列表，我们需要保证排序的唯一性，时间有时候不能保证，所以我们会主动追加根据id排序
            if (!ListUtil.contains(order, (t) -> t.equalsIgnoreCase(EntityObject.IdPropertyName)))
                _orderBy = String.format("%s,%s asc", _orderBy, EntityObject.IdPropertyName);
        }
    }

    public void orderBy(String orderBy) {
        _orderBy = String.format("order by %s", orderBy);
    }

    private String _groupBy = StringUtil.empty();

    public void groupBy(String format, Object... args) {
        _groupBy = StringUtil.format(format, args);
    }

    private String getFirstPageCT() {
        var temp = StringUtil.format("select {0} from {1} {2} {3} {4}", _select, _from, _condition, getGroupBy(), _orderBy);
        temp = DBUtil.addQualifier(temp, _table);
        return String.format("%s LIMIT @data_length;", temp);
    }

    private String getPageCT() {
        String temp = StringUtil.format(
                "select row_number() over({0}) as ind,{3} from {1} {2} {4} {0}",
                _orderBy, _from, _condition, _select, getGroupBy());
        temp = DBUtil.addQualifier(temp, _table);
        String aSql = String.format("%s LIMIT @data_end ", temp);

        return String.format("select %s from (%s) as a where a.ind > @data_start and a.ind <= @data_end",
                _select, aSql);
    }

    private String getGroupBy() {
        if (StringUtil.isNullOrEmpty(_groupBy))
            return StringUtil.empty();
        return String.format("group by %s", _groupBy);
    }

    //region 模板代码


    /// <summary>
    /// 获取翻页代码
    /// </summary>
    /// <param name="pageIndex">从0开始的下标</param>
    /// <param name="pageSize"></param>
    /// <returns></returns>
    public String getCode(int pageIndex, int pageSize) {
        int count = pageSize; // count为返回的记录条数。例：offset:2, count:5 即意为从第3条记录开始的5条记录。
        if (pageIndex == 0) return StringUtil.format(this._firstPageCode, count);

        int start = pageIndex * pageSize; // 数据的开始位置，为从第几条（start+1）记录开始，
        int end = start + count; // 数据的结束位置
        return StringUtil.format(this._otherPageCode, start, count, end);
    }

    //endregion
}
