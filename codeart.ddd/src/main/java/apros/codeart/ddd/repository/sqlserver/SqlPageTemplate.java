package apros.codeart.ddd.repository.sqlserver;

import java.text.MessageFormat;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.repository.access.internal.SqlDefinition;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public class SqlPageTemplate {
	public SqlPageTemplate() {
	}

	private String _select = StringUtil.empty();

	public void select(String format, Object... args) {
		_select = MessageFormat.format(format, args);
	}

	private String _from = StringUtil.empty();

	public void from(String format, Object... args) {
		_from = MessageFormat.format(format, args);
	}

	private StringBuilder _condition = new StringBuilder();

	public void where(String format, Object... args) {
		String cmd = _condition.length() > 0 ? " and " : " where ";
		_condition.append(cmd);
		StringUtil.appendMessageFormat(_condition, format, args);
	}

	private String _orderBy = StringUtil.empty();

	public void orderBy(SqlDefinition definition) {
		_orderBy = definition.order();
		var order = definition.columns().order();
		// 对于翻页列表，我们需要保证排序的唯一性，时间有时候不能保证，所以我们会主动追加根据id排序
		if (!ListUtil.contains(order, (t) -> t.equalsIgnoreCase(EntityObject.IdPropertyName)))
			_orderBy = String.format("%s,%s asc", _orderBy, EntityObject.IdPropertyName);
	}

	public void orderBy(String orderBy) {
		_orderBy = String.format("order by %s", orderBy);
	}

	private String _groupBy = StringUtil.empty();

	public void groupBy(String format, Object... args) {
		_groupBy = MessageFormat.format(format, args);
	}

	private String getFirstPageCT() {
		return MessageFormat.format("select top @data_length * from {0} {1} {2};", getFrom(), _condition, _orderBy);
	}

	private String getPageCT() {
		String from = getFrom();

		String aSql = MessageFormat.format(
				"select top @data_end {0} as pk,row_number() over({1}) as ind from {2} {3} {1}",
				EntityObject.IdPropertyName, _orderBy, from, _condition);
		String tSql = String.format("select pk from (%s) as a where a.ind > @data_start and a.ind <= (@data_end)",
				aSql);

		StringBuilder sql = new StringBuilder();
		StringUtil.appendLine(sql, "select * from");
		StringUtil.appendFormat(sql, "    (%s)", tSql);
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, "    as t");
		StringUtil.appendLine(sql, "inner join");

		StringUtil.appendFormat(sql, "    (select * from %s)", from);
		StringUtil.appendLine(sql);
		StringUtil.appendLine(sql, "     as t2");
		StringUtil.appendFormat(sql, "on t.pk=t2.%s", EntityObject.IdPropertyName);
		StringUtil.appendFormat(sql, " %s;", _orderBy);
		return sql.toString();
	}

	private String getFrom() {
		return String.format("(select %s from %s %s) as _tb", _select, _from, getGroupBy());
	}

	private String getGroupBy() {
		if (StringUtil.isNullOrEmpty(_groupBy))
			return StringUtil.empty();
		return String.format("group by %s", _groupBy);
	}

//	#region 模板代码

	private String _templateCode;

	public String templateCode() {
		if (_templateCode == null) {
			_templateCode = getTemplateCode();
		}
		return _templateCode;
	}

	public String getTemplateCode() {
		StringBuilder sql = new StringBuilder();
		StringUtil.appendLine(sql, "if(@data_start=0)");
		StringUtil.appendLine(sql, getFirstPageCT());
		StringUtil.appendLine(sql, "else");

		sql.append(getPageCT());

		StringUtil.replaceAll(sql, "@data_start", "{0}"); // 替换成格式化参数
		StringUtil.replaceAll(sql, "@data_length", "{1}"); // 替换成格式化参数
		StringUtil.replaceAll(sql, "@data_end", "{2}"); // 替换成格式化参数

		return sql.toString();
	}

	/// <summary>
	/// 获取翻页代码
	/// </summary>
	/// <param name="pageIndex">从1开始的下标</param>
	/// <param name="pageSize"></param>
	/// <returns></returns>
	public String getCode(int pageIndex, int pageSize) {
		int start = (pageIndex - 1) * pageSize; // 数据的开始位置，为从第几条（start+1）记录开始，
		int count = pageSize; // count为返回的记录条数。例：offset:2, count:5 即意为从第3条记录开始的5条记录。
		int end = start + count; // 数据的结束位置
		return MessageFormat.format(this.templateCode(), start, count, end);
	}

//	#endregion

	@Override
	public String toString() {
		return this.templateCode();
	}
}
