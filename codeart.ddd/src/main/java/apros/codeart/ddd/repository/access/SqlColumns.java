package apros.codeart.ddd.repository.access;

import java.util.List;

import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public class SqlColumns {

	private Iterable<String> _select;

	public Iterable<String> select() {
		return _select;
	}

	private Iterable<String> _where;

	public Iterable<String> where() {
		return _where;
	}

	private Iterable<String> _order;

	public Iterable<String> order() {
		return _order;
	}

	/// <summary>
	/// 判定查询是否涉及到<paramref name="fieldName"/>
	/// </summary>
	/// <param name="fieldName"></param>
	/// <returns></returns>
	public boolean contains(String fieldName) {
		return this.isAll() || this.specified(fieldName);
	}

	/**
	 * 确实手工指定了某个字段,与Contains不同,当 select * 时，Contains返回的是true
	 * 
	 * @param fieldName
	 * @return
	 */
	public boolean specified(String fieldName) {

		return StringUtil.containsIgnoreCase(_select, fieldName) || StringUtil.containsIgnoreCase(_where, fieldName)
				|| StringUtil.containsIgnoreCase(_order, fieldName);
	}

	private boolean _isAll;

	public boolean isAll() {
		return _isAll;
	}

	public SqlColumns(Iterable<String> select, Iterable<String> where, Iterable<String> order) {
		_select = map(select); // 将对象关系链改成 _
		_where = map(where);
		_order = map(order);
		_isAll = StringUtil.contains(_select, "*");
	}

	private static Iterable<String> map(Iterable<String> columns) {
		return ListUtil.map(columns, (t) -> t.replace(".", "_"));
	}

	public static final SqlColumns Empty = new SqlColumns(ListUtil.empty(), ListUtil.empty(), ListUtil.empty());

	public static final SqlColumns All = new SqlColumns(List.of("*"), ListUtil.empty(), ListUtil.empty());
}
