package apros.codeart.dto;

import static apros.codeart.util.StringUtil.isNullOrEmpty;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;

class QueryExpression {

	private boolean _isSelf;

	public boolean isSelf() {
		return _isSelf;
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

	private boolean _isEmpty;

	public boolean isEmpty() {
		return _isEmpty;
	}

	private QueryExpression(String queryString) {
		this._isEmpty = isNullOrEmpty(queryString);
		this._isSelf = queryString.equals("*");
		this._segment = getSegment(queryString);
		this._next = parseNext(queryString);
	}

	private String getSegment(String queryString) {
		var segment = queryString;
		int dot = queryString.indexOf('.');
		if (dot > -1)
			segment = queryString.substring(0, dot);
		return segment;
	}

	private QueryExpression parseNext(String queryString) {
		int dot = queryString.indexOf('.');
		if (dot == -1)
			return null;
		var nextQueryString = queryString.substring(dot + 1);
		return QueryExpression.create(nextQueryString);
	}

	private static Function<String, QueryExpression> _getExpression = LazyIndexer.init((queryString) -> {
		return new QueryExpression(queryString);
	});

	public static QueryExpression create(String queryString) {
		return _getExpression.apply(queryString);
	}

}