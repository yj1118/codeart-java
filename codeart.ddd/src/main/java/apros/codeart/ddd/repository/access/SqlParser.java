package apros.codeart.ddd.repository.access;

import static apros.codeart.i18n.Language.strings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

final class SqlParser {
	private SqlParser() {
	}

	/// <summary>
	/// 分析sql语句，获得参与查询的列信息
	/// </summary>
	/// <param name="sqlSelect"></param>
	public static SqlColumns parse(String sql) {
		return _getColumns.apply(sql);
	}

	private static Function<String, SqlColumns> _getColumns = LazyIndexer.init((sql) -> {

		try {

			Statement statement = CCJSqlParserUtil.parse(sql);

			if (statement instanceof Select) {
				Select selectStatement = (Select) statement;
				PlainSelect plain = (PlainSelect) selectStatement.getSelectBody();

				var select = getSelect(plain, sql);
				var where = getWhere(plain, sql);
				var order = getOrder(plain, sql);
				return new SqlColumns(select, where, order);
			}
			throw new IllegalStateException(strings("codeart.ddd", "SQLFormatError", sql));
		} catch (Exception ex) {
			throw new IllegalStateException(strings("codeart.ddd", "SQLFormatError", sql));
		}

	});

	private static Iterable<String> getSelect(PlainSelect plainSelect, String sql) {
		List<SelectItem> selectItems = plainSelect.getSelectItems();
		var columnNames = ListUtil.map(selectItems, (item) -> {

			if (item instanceof SelectExpressionItem) {
				SelectExpressionItem expressionItem = (SelectExpressionItem) item;
				Expression expression = expressionItem.getExpression();

				String fieldName = null;
				String aliasName = null;

				if (expression instanceof Column) {
					Column column = (Column) expression;
					fieldName = column.getColumnName(); // Get the column name
				} else {
					fieldName = expression.toString(); // Get the expression as string for complex expressions
				}

				// 对于 name as xxx的形式，我们只取name
//				Alias alias = expressionItem.getAlias();
//				if (alias != null) {
//					aliasName = alias.getName(); // Get the alias name
//					return fieldName + (aliasName != null ? " AS " + aliasName : "");
//				}

				return SqlStatement.unQualifier(fieldName);
			}

			throw new IllegalStateException(strings("codeart.ddd", "SQLFormatError", sql));

		});

		return StringUtil.distinctIgnoreCase(columnNames);

	}

	private static Iterable<String> getWhere(PlainSelect plainSelect, String sql) {

		Expression where = plainSelect.getWhere();

		List<String> columnNames = new ArrayList<>();
		where.accept(new ExpressionVisitorAdapter() {
			@Override
			public void visit(Column column) {
				columnNames.add(column.getFullyQualifiedName());
			}

			@Override
			public void visit(ExpressionList expressionList) {
				super.visit(expressionList);
				expressionList.getExpressions().forEach(expr -> expr.accept(this));
			}
		});

		var fields = ListUtil.map(columnNames, (item) -> {
			return SqlStatement.unQualifier(item);
		});

		return StringUtil.distinctIgnoreCase(fields);
	}

	private static Iterable<String> getOrder(PlainSelect plainSelect, String sql) {
		List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
		if (orderByElements == null)
			return new ArrayList<String>(0);

		ArrayList<String> columns = new ArrayList<String>(orderByElements.size());

		for (OrderByElement orderBy : orderByElements) {
			Column column = (Column) orderBy.getExpression();
			String columnName = column.getColumnName();
			columns.add(SqlStatement.unQualifier(columnName));
		}

		return StringUtil.distinctIgnoreCase(columns);
	}
//
//	private static Object GetExpression(Type targetType, object target, string propertyName) {
//		if (targetType.ResolveProperty(propertyName) == null)
//			return null;
//		return target.GetPropertyValue(propertyName);
//	}
//
//	private static string GetSelectString(TSqlFragment statement) {
//		if (statement == null)
//			return string.Empty;
//
//		for (int i = statement.FirstTokenIndex; i <= statement.LastTokenIndex; i++) {
//			// 对于 name as xxx的形式，我们只取name
//			var name = statement.ScriptTokenStream[i].Text;
//			if (name.StartsWith("["))
//				return name.Substring(1, name.Length - 2);
//			return name;
//		}
//		return string.Empty;
//	}
//
//	private static string GetString(TSqlFragment statement) {
//		if (statement == null)
//			return string.Empty;
//
//		StringBuilder code = new StringBuilder();
//
//		for (int i = statement.FirstTokenIndex; i <= statement.LastTokenIndex; i++) {
//			code.Append(statement.ScriptTokenStream[i].Text);
//		}
//
//		return code.ToString();
//	}
//todo  有可能要对特别情况做出额外处理，这里要用数据库代理来扩展兼容，而不是直接判断对"datediff"处理
//	private static IEnumerable<string> GetFields(FunctionCall func) {
//		List<string> fields = new List<string>();
//
//		for (int i = func.FirstTokenIndex; i <= func.LastTokenIndex; i++) {
//			var token = func.ScriptTokenStream[i];
//			if (token.TokenType == TSqlTokenType.Identifier) {
//				fields.Add(token.Text);
//			}
//		}
//		if (fields.Count == 0)
//			return fields;
//
//		var funcName = fields[0].ToLower();
//		switch (funcName) {
//		case "datediff": {
//			var column = fields[2]; // 该函数仅提取第3个参数作为列的名称
//			fields.Clear();
//			fields.Add(column);
//		}
//			break;
//		default: {
//			throw new DataAccessException(string.Format(Strings.UnrecognizedSqlFunction, funcName));
//		}
//		}
//
//		return fields;
//	}

}
