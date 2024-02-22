package apros.codeart.dto;

import static apros.codeart.util.StringUtil.isNullOrEmpty;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

class QueryExpression {

	private boolean _onlySelf;

	public boolean onlySelf() {
		return _onlySelf;
	}

	private String _segment;

	/**
	 * 该表达式对应的路径片段
	 * 
	 * @return
	 */
	public String getSegment() {
		return _segment;
	}

	private QueryExpression _next;

	public QueryExpression getNext() {
		return _next;
	}

	public boolean hasNext() {
		return _next != null;
	}

	private boolean _isEmpty;

	public boolean isEmpty() {
		return _isEmpty;
	}

	private QueryExpression(String queryString) {
		this._isEmpty = isNullOrEmpty(queryString);
		this._onlySelf = queryString.equals("*");
		this._segment = getSegment(queryString);
		this._next = parseNext(queryString);
	}

	private String getSegment(String queryString) {
		var segment = queryString;
		int dot = queryString.indexOf('.');
		if (dot > -1)
			segment = StringUtil.substr(queryString, 0, dot);
		return segment;
	}

	private QueryExpression parseNext(String queryString) {
		int dot = queryString.indexOf('.');
		if (dot == -1)
			return null;
		var nextQueryString = StringUtil.substr(queryString, dot + 1);
		return QueryExpression.create(nextQueryString);
	}

	private static Function<String, QueryExpression> _getExpression = LazyIndexer.init((queryString) -> {
		return new QueryExpression(queryString);
	});

	public static QueryExpression create(String queryString) {
		return _getExpression.apply(queryString);
	}

}
