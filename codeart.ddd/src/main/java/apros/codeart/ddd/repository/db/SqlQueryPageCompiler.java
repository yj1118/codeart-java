package apros.codeart.ddd.repository.db;

import apros.codeart.ddd.repository.access.IQueryPageCompiler;
import apros.codeart.ddd.repository.access.QueryPageCode;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.SafeAccess;
import apros.codeart.util.StringUtil;

import java.util.function.Function;


@SafeAccess
public class SqlQueryPageCompiler implements IQueryPageCompiler {

    public static final IQueryPageCompiler INSTANCE = new SqlQueryPageCompiler();

    private SqlQueryPageCompiler() {
    }

    @Override
    public String buildPage(QueryPageCode code, int pageIndex, int pageSize) {
        var result = _getPageResult.apply(code);
        int count = pageSize; // count为返回的记录条数。例：offset:2, count:5 即意为从第3条记录开始的5条记录。
        if (pageIndex == 0) return StringUtil.format(result.firstPageCode(), count);

        int start = pageIndex * pageSize; // 数据的开始位置，为从第几条（start+1）记录开始，
        int end = start + count; // 数据的结束位置
        return StringUtil.format(result.otherPageCode(), start, count, end);
    }

    @Override
    public String buildCount(QueryPageCode code) {
        return _getPageCount.apply(code);
    }

    private record CompileResult(String firstPageCode, String otherPageCode) {

    }

    private static final Function<QueryPageCode, CompileResult> _getPageResult = LazyIndexer.init((code) -> {
        var tableSql = getPageTableSql(code);

        var firstPageCode = getFirstPageCode(tableSql, code);
        var otherPageCode = getOtherPageCode(tableSql, code);

        return new CompileResult(firstPageCode, otherPageCode);
    });

    private static final Function<QueryPageCode, String> _getPageCount = LazyIndexer.init((code) -> {
        var tableSql = getPageTableSql(code);
        var bottomSql = "SELECT COUNT(*) FROM PageTableCTE";
        return String.format("%s%s%s", tableSql, System.lineSeparator(), bottomSql);
    });


    private static String getPageTableSql(QueryPageCode code) {
        String tableSql = code.tableSql();

        StringBuilder sb = new StringBuilder();
        StringUtil.appendFormat(sb, "WITH PageTableCTE AS (");
        StringUtil.appendLine(sb);
        StringUtil.appendLine(sb, String.format("SELECT %s,row_number() over(ORDER BY %s) as __ind FROM", code.selectSql(), code.orderSql()));
        StringUtil.appendLine(sb, tableSql);
        StringUtil.append(sb, ")");

        return sb.toString();
    }

    private static String getFirstPageCT(QueryPageCode code) {
        return "SELECT * FROM PageTableCTE ORDER BY __ind asc LIMIT @data_length";
    }

    private static String getPageCT(QueryPageCode code) {
        String temp = String.format(
                "SELECT * from PageTableCTE %s LIMIT @data_end",
                code.orderSql());

        return String.format("SELECT %s from (%s) as a where a.__ind > @data_start and a.__ind <= @data_end",
                code.selectSql(), temp);
    }

    private static String getFirstPageCode(String tableSql, QueryPageCode code) {
        var bottomSql = getFirstPageCT(code);
        bottomSql = bottomSql.replaceAll("@data_length", "{0}");// 替换成格式化参数
        return String.format("%s%s%s", tableSql, System.lineSeparator(), bottomSql);
    }

    private static String getOtherPageCode(String tableSql, QueryPageCode code) {
        var bottomSql = getPageCT(code);
        bottomSql = bottomSql.replaceAll("@data_start", "{0}");// 替换成格式化参数
        bottomSql = bottomSql.replaceAll("@data_length", "{1}");// 替换成格式化参数
        bottomSql = bottomSql.replaceAll("@data_end", "{2}");// 替换成格式化参数

        return String.format("%s%s%s", tableSql, System.lineSeparator(), bottomSql);
    }

    //region 模板代码

}