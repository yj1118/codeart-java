package apros.codeart.ddd.repository.db;

import apros.codeart.ddd.QueryLevel;
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
        return StringUtil.format(result.otherPageCode(), start, end);
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

        String coreSql = String.format("%s%s%s", String.format("SELECT %s,row_number() over(ORDER BY %s) as __ind FROM", code.selectSql(), code.orderSql()),
                System.lineSeparator(),
                tableSql);

        if (code.table() != null) {
            //如果指定了table信息，那么就根据table的元数据格式化下sql
            coreSql = DBUtil.format(coreSql, code.table(), QueryLevel.NONE);
        }


        StringBuilder sb = new StringBuilder();
        StringUtil.appendFormat(sb, "WITH PageTableCTE AS (");
        StringUtil.appendLine(sb);
        StringUtil.appendLine(sb, coreSql);
        StringUtil.appendLine(sb, "LIMIT @data_end");
        StringUtil.append(sb, ")");

        return sb.toString();
    }

    private static String getFirstPageCT(QueryPageCode code) {
        return "SELECT * FROM PageTableCTE ORDER BY __ind asc";
    }

    private static String getPageCT(QueryPageCode code) {
        return "SELECT * FROM PageTableCTE where __ind > @data_start and __ind <= @data_end ORDER BY __ind asc";
    }

    private static String getFirstPageCode(String tableSql, QueryPageCode code) {
        tableSql = tableSql.replaceAll("@data_end", "{0}");// 替换成格式化参数
        var bottomSql = getFirstPageCT(code);
        return String.format("%s%s%s", tableSql, System.lineSeparator(), bottomSql);
    }

    private static String getOtherPageCode(String tableSql, QueryPageCode code) {
        tableSql = tableSql.replaceAll("@data_end", "{1}");// 替换成格式化参数
        var bottomSql = getPageCT(code);
        bottomSql = bottomSql.replaceAll("@data_start", "{0}");// 替换成格式化参数
        bottomSql = bottomSql.replaceAll("@data_end", "{1}");// 替换成格式化参数

        return String.format("%s%s%s", tableSql, System.lineSeparator(), bottomSql);
    }

    //region 模板代码

}