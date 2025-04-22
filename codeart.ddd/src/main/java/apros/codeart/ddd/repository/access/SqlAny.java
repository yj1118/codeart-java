package apros.codeart.ddd.repository.access;

import apros.codeart.util.LazyIndexer;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlAny {
    private final String _paramName;

    private final Pattern _regex;
    private final Function<String, Placeholder> _getPlaceholder;

    public record Placeholder(String symbol, String content) {

    }

    // 纯用正则有缺陷：
//    AND ("Dimensions"."Id" IN (@dimensionIds))
//    这时候匹配的结果就是 AND ("Dimensions"."Id" IN (@dimensionIds)，缺少一个)
//    // 全部参数的构造函数
//    public SqlAny(int index, String paramName) {
//        this._paramName = paramName;
//        this._regex = Pattern.compile(String.format("'__@[a-zA-Z0-9_]+_any_%s'\\s*=\\s*'__@[a-zA-Z0-9_]+_any_%s'\\s*AND\\s*\\((.+?)\\)", index, index), Pattern.CASE_INSENSITIVE);
//
//        this._getPlaceholder = LazyIndexer.init((commandText) -> {
//            Matcher matcher = _regex.matcher(commandText);
//            if (matcher.find()) {
//                String symbol = matcher.group(0);
//                String content = matcher.group(1);
//                return new Placeholder(symbol, content);
//            }
//            return null;
//        });
//    }

    /**
     * 已改为正则+代码分析，避免 AND ("Dimensions"."Id" IN (@dimensionIds)) 纯正则匹配的结果就是 AND ("Dimensions"."Id" IN (@dimensionIds)，缺少一个)
     *
     * @param index
     * @param paramName
     */
    public SqlAny(int index, String paramName) {
        this._paramName = paramName;

        // 用于查找 AND 后的起始 (
        this._regex = Pattern.compile(String.format("'__@[a-zA-Z0-9_]+_any_%s'\\s*=\\s*'__@[a-zA-Z0-9_]+_any_%s'\\s*AND\\s*\\(", index, index), Pattern.CASE_INSENSITIVE);

        this._getPlaceholder = LazyIndexer.init((commandText) -> {
            Matcher matcher = _regex.matcher(commandText);
            if (matcher.find()) {
                int start = matcher.start(); // 匹配整段起始
                int contentStart = matcher.end() - 1; // 指向 '('

                int count = 0;
                int end = contentStart;

                for (; end < commandText.length(); end++) {
                    char c = commandText.charAt(end);
                    if (c == '(') count++;
                    else if (c == ')') count--;

                    if (count == 0) break;
                }

                if (count == 0) {
                    String symbol = commandText.substring(start, end + 1); // 包含 AND (...) 整体
                    String content = commandText.substring(contentStart + 1, end); // 括号内内容，不含 ()
                    return new Placeholder(symbol, content);
                }
            }

            return null;
        });
    }


    // Getter方法
    public String paramName() {
        return _paramName;
    }

    public SqlAny.Placeholder getPlaceholder(String commandText) {
        return _getPlaceholder.apply(commandText);
    }

//
//    // 更新占位符方法
//    public void tryUpdate(String commandText) {
//        if (this._placeholderIsUpdated) return;
//
//        Matcher matcher = _regex.matcher(commandText);
//        if (matcher.find()) {
//            this._placeholder = matcher.group(0);
//            this._content = matcher.group(1);
//        }
//        this._placeholderIsUpdated = true;
//    }


}
