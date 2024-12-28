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

    // 全部参数的构造函数
    public SqlAny(int index, String paramName) {
        this._paramName = paramName;
//        this._placeholder = placeholder;
//        this._content = content;
        this._regex = Pattern.compile(String.format("'__@[a-zA-Z0-9_]+_any_%s'\\s*=\\s*'__@[a-zA-Z0-9_]+_any_%s'\\s*AND\\s*\\((.+?)\\)", index, index), Pattern.CASE_INSENSITIVE);

        this._getPlaceholder = LazyIndexer.init((commandText) -> {
            Matcher matcher = _regex.matcher(commandText);
            if (matcher.find()) {
                String symbol = matcher.group(0);
                String content = matcher.group(1);
                return new Placeholder(symbol, content);
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
