package apros.codeart.dto;

import static apros.codeart.util.StringUtil.isNullOrEmpty;

import java.util.function.Function;

import apros.codeart.util.LazyIndexer;
import apros.codeart.util.StringUtil;

public class QueryExpression {

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

        if (queryString.startsWith("@")) { // @可以把.当作一个整体表达式
            var segment = StringUtil.substr(queryString, 1);
            int dot = segment.indexOf('@');
            if (dot > -1)
                segment = StringUtil.substr(segment, 0, dot - 1);
            return segment;
        } else {
            var segment = queryString;

            int dot = queryString.indexOf('.');
            if (dot > -1)
                segment = StringUtil.substr(queryString, 0, dot);
            return segment;
        }
    }

    private QueryExpression parseNext(String queryString) {
        if (queryString.startsWith("@")) {
            var segment = StringUtil.substr(queryString, 1);
            int dot = segment.indexOf('@');
            if (dot > -1) {
                segment = StringUtil.substr(segment, dot);
                return QueryExpression.create(segment);
            }
            // @xxx.xx 后面没有@了，所以next为null
            return null;
        } else {
            int dot = queryString.indexOf('.');
            if (dot == -1)
                return null;
            var nextQueryString = StringUtil.substr(queryString, dot + 1);
            return QueryExpression.create(nextQueryString);
        }
    }

    private static final Function<String, QueryExpression> _getExpression = LazyIndexer.init((queryString) -> {
        return new QueryExpression(queryString);
    });

    public static QueryExpression create(String queryString) {
        if (queryString == null)
            queryString = StringUtil.empty();
        return _getExpression.apply(queryString);
    }

}
