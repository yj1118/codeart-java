package apros.codeart.ddd.repository.access.internal;

import java.util.ArrayList;
import java.util.function.Function;

import com.google.common.collect.Iterables;

import apros.codeart.ddd.EntityObject;
import apros.codeart.ddd.MapData;
import apros.codeart.ddd.repository.access.DataTable;
import apros.codeart.ddd.repository.access.SqlColumns;
import apros.codeart.i18n.Language;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public class SqlDefinition {

	private String _top;

	/**
	 * top限定语句
	 * 
	 * @return
	 */
	public String top() {
		return _top;
	}

	private ArrayList<String> _selectFields;

	/**
	 * select语句指定查询的字段名称
	 * 
	 * @return
	 */
	public Iterable<String> selectFields() {
		return _selectFields;
	}

	private ArrayList<String> _inners;

	/**
	 * 需要预加入的数据
	 * 
	 * @return
	 */
	public Iterable<String> inners() {
		return _inners;
	}

	public boolean hasInner() {
		return _inners.size() > 0;
	}

	private String _outputFileds;

	/**
	 * 
	 * 在查询中需要输出的字段名集合
	 * 
	 * @return
	 */
	public String outputFileds() {
		return _outputFileds;
	}

	public String getFieldsSql() {
		if (this.outputFileds() == null) {
			if (_selectFields.size() == 0)
				_outputFileds = "*";
			else {
				// where涉及到的字段内置到GetObjectSql中，所以不必考虑
				ArrayList<String> temp = new ArrayList<String>();
				ListUtil.addRange(temp, this.columns().select());
				ListUtil.addRange(temp, this.columns().order());

				var fields = StringUtil.distinctIgnoreCase(temp);

				StringBuilder sql = new StringBuilder();

				for (var field : fields) {
					sql.append(SqlStatement.qualifier(field));
					sql.append(",");
				}

				if (sql.length() > 0)
					StringUtil.removeLast(sql);
				_outputFileds = sql.toString();
			}
		}
		return _outputFileds;
	}

	/**
	 * 是否指定了加载哪些字段
	 * 
	 * @return
	 */
	public boolean isSpecifiedField() {
		return !this.columns().isAll();
	}

	private SqlCondition _condition;

	/**
	 * 查询条件
	 * 
	 * @return
	 */
	public SqlCondition condition() {
		return _condition;
	}

	private String _order;

	/**
	 * 排序
	 * 
	 * @return
	 */
	public String order() {
		return _order;
	}

	private SqlColumns _columns;

	public SqlColumns columns() {
		return _columns;
	}

	private String _key;

	public String key() {
		return _key;
	}

	/// <summary>
	/// 是否为自定义sql，这表示由程序员自己翻译执行的sql语句
	/// </summary>
	public boolean isCustom() {
		return !StringUtil.isNullOrEmpty(this.key());
	}

	public boolean isNative() {
		return StringUtil.isNullOrEmpty(_nativeSql);
	}

	private String _nativeSql;

	public String nativeSql() {
		return _nativeSql;
	}

	public void nativeSql(String value) {
		_nativeSql = value;
	}

	private SqlDefinition() {
		_top = StringUtil.empty();
		_selectFields = new ArrayList<String>();
		_condition = SqlCondition.Empty;
		_order = StringUtil.empty();
		_columns = SqlColumns.Empty;
		_inners = new ArrayList<String>();
		_key = StringUtil.empty();
	}

	private SqlDefinition(SqlColumns columns) {
		this();
		_columns = columns;
	}

	/// <summary>
	/// 对象链是否包括在inner表达式中,对象链的格式是 a_b_c
	/// </summary>
	/// <param name="ObjectChain"></param>
	/// <returns></returns>
	public boolean containsInner(String objectChain) {
		if (_inners.size() == 0)
			return false;

		for (var inner : this.inners()) {
			if (_getInnerChaint.apply(inner).equalsIgnoreCase(objectChain))
				return true;
		}

		return false;
	}

	private static Function<String, String> _getInnerChaint = LazyIndexer.init((inner) -> {
		return inner.replace('.', '_');
	});

	public boolean containsChain(String objectChain) {
		if (this.isEmpty())
			return false;

		var objectChainOffset = _getObjectChainOffset.apply(objectChain);
		return containsChain(this.columns().select(), objectChainOffset)
				|| containsChain(this.columns().where(), objectChainOffset)
				|| containsChain(this.columns().order(), objectChainOffset);
	}

	private static Function<String, String> _getObjectChainOffset = LazyIndexer.init((objectChain) -> {
		return String.format("%s_", objectChain);
	});

	private static boolean containsChain(Iterable<String> fields, String target) {
		for (var field : fields) {
			if (StringUtil.indexOfIgnoreCase(field, target) > -1)
				return true;
		}
		return false;
	}

	/// <summary>
	/// 判断表<paramref name="table"/>的字段是否出现在当前sql定义中，（除了ID以外的字段）
	/// </summary>
	/// <param name="table"></param>
	/// <returns></returns>
	public boolean containsExceptId(DataTable table) {
		if (this.isEmpty())
			return false;

		var fields = table.fields();
		for (var field : fields) {
			if (field.name() == EntityObject.IdPropertyName)
				continue;
			if (this.columns().contains(field.name()))
				return true;
		}
		return false;
	}

	public boolean containsField(String fieldName) {
		return this.columns().contains(fieldName);
	}

	/// <summary>
	/// 确实手工指定了某个字段,与ContainsField不同,当 select * 时，Contains返回的是true
	/// </summary>
	/// <param name="fieldName"></param>
	/// <returns></returns>
	public boolean specifiedField(String fieldName) {
		return this.columns().specified(fieldName);
	}

	private boolean _isEmpty;

	/// <summary>
	///
	/// </summary>
	public boolean isEmpty() {
		return _isEmpty;
	}

	private static boolean confirmEmpty(SqlDefinition def) {
		return def.top().length() == 0 && Iterables.size(def.selectFields()) == 0 && def.condition().isEmpty()
				&& def.order().length() == 0;
	}

	public static SqlDefinition create(String expression) {
		if (StringUtil.isNullOrEmpty(expression))
			return Empty;
		return _create.apply(expression);
	}

	private static Function<String, SqlDefinition> _create = LazyIndexer.init((expression) -> {
		SqlDefinition define = new SqlDefinition();
		final String sqlTip = "[sql]";
		if (expression.startsWith("[sql]")) {
			define.nativeSql(StringUtil.substr(expression, sqlTip.length()));
		} else {
			var subs = collectSubs(expression);

			for (var sub : subs) {
				var exp = pretreatment(sub);
				if (StringUtil.isNullOrEmpty(exp))
					continue;

				if (isTop(exp))
					define._top = exp;
				else if (isOrder(exp))
					define._order = exp;
				else if (isSelect(exp))
					define._selectFields = getFields(exp);
				else if (isKey(exp))
					define._key = getKey(exp);
				else if (isInner(exp))
					define._inners = getInners(exp);
				else {
					define._condition = new SqlCondition(exp); // 默认为条件
				}
			}

			define._isEmpty = confirmEmpty(define); // 缓存结果，运行时不必再运算
			define._columns = getColumns(define);
		}
		return define;

	});

	private static SqlColumns getColumns(SqlDefinition define) {
		var mockSql = String.format("select %s from tempTable %s %s", getSelectFields(define),
				(define.condition().isEmpty() ? StringUtil.empty()
						: String.format("where %s", define.condition().probeCode())),
				define.order());
		return SqlParser.parse(mockSql);
	}

	private static String getSelectFields(SqlDefinition define) {
		if (Iterables.size(define.selectFields()) == 0)
			return "*";

		var sql = new StringBuilder();
		for (var field : define.selectFields()) {
			sql.append(SqlStatement.qualifier(field));
			sql.append(",");
		}
		if (sql.length() > 0)
			StringUtil.removeLast(sql);

		return sql.toString();
	}

	/// <summary>
	/// 预处理
	/// </summary>
	/// <returns></returns>
	private static String pretreatment(String expression) {
		if (expression.startsWith("[")) {
			expression = StringUtil.substr(expression, expression.length() - 2);
		}

		expression = expression.trim();
		return expression.replace(".", "_"); // 将属性路径中的 . 转换成字段分隔 _
	}

	private static boolean isTop(String expression) {
		return StringUtil.startsWithIgnoreCase(expression, "top ");
	}

	private static boolean isKey(String expression) {
		return StringUtil.startsWithIgnoreCase(expression, "key ");
	}

	private static boolean isInner(String expression) {
		return StringUtil.startsWithIgnoreCase(expression, "inner ");
	}

	/// <summary>
	/// 使用select 指定需要查询的字段名称
	/// </summary>
	/// <param name="expression"></param>
	/// <returns></returns>
	private static boolean isSelect(String expression) {
		return StringUtil.startsWithIgnoreCase(expression, "select ");
	}

	private static final int _selectLength = "select ".length();

	private static ArrayList<String> getFields(String expression) {
		int startIndex = _selectLength;
		var exp = expression.substring(startIndex).trim();
		if (StringUtil.isNullOrEmpty(exp))
			return new ArrayList<String>();

		return ListUtil.map(exp.split(","), (field) -> {
			return field.trim();
		});
	}

	private static final int _keyLength = "key ".length();

	private static String getKey(String expression) {
		int startIndex = _keyLength;
		return expression.substring(startIndex).trim();
	}

	private static boolean isOrder(String expression) {
		return StringUtil.startsWithIgnoreCase(expression, "order by");
	}

	private static final int _innerLength = "inner ".length();

	private static ArrayList<String> getInners(String expression) {
		int startIndex = _innerLength;
		var temp = expression.substring(startIndex).trim();

		return ListUtil.asList(temp.split(","));
	}

//	#region 子表达式

	/// <summary>
	/// 收集子表达式
	/// </summary>
	/// <param name="expression"></param>
	/// <returns></returns>
	private static ArrayList<String> collectSubs(String expression) {
		ArrayList<String> subs = new ArrayList<String>();
		int pointer = 0;
		while (pointer < expression.length()) {
			var sub = findSub(expression, pointer);
			subs.add(sub);
			pointer += sub.length();
		}
		return subs;
	}

	/// <summary>
	/// 找到子表达式
	/// </summary>
	/// <param name="expression"></param>
	/// <param name="startIndex"></param>
	/// <returns></returns>
	private static String findSub(String expression, int startIndex) {
		if (expression.charAt(startIndex) != '[') {
			var endIndex = expression.indexOf('[', startIndex);
			if (endIndex > 0) {
				// 格式：aaa[bbbb]
				return StringUtil.substr(expression, startIndex, endIndex - startIndex);
			} else {
				// 格式：aaa
				return StringUtil.substr(expression, startIndex);
			}
		} else {
			var endIndex = expression.indexOf(']', startIndex);
			if (endIndex > 0) {
				// 格式：[aaa][bbbb]
				return StringUtil.substr(expression, startIndex, endIndex - startIndex + 1);
			} else {
				// 格式：[aaa
				throw new IllegalStateException(
						Language.strings("codeart.ddd", "QueryExpressionMalformed", expression));
			}
		}
	}

//	#endregion
//
//	#

//	region 处理命令文本

	public String process(String commandText, MapData param) {
		if (this.isNative())
			return this.nativeSql();
		if (StringUtil.isNullOrEmpty(commandText) || this.isEmpty())
			return commandText;
		return this.condition().process(commandText, param);
	}

//	#endregion

	public static final SqlDefinition Empty = new SqlDefinition();

	/// <summary>
	/// 除根之外都加载
	/// </summary>
	public static final SqlDefinition All = new SqlDefinition(SqlColumns.All);

}
