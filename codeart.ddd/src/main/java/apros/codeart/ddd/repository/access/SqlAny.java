package apros.codeart.ddd.repository.access;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlAny {
    private final String _paramName;
    private String _placeholder;
    private String _content;
    private boolean _placeholderIsUpdated;

    // 全部参数的构造函数
    public SqlAny(String paramName, String placeholder, String content) {
        this._paramName = paramName;
        this._placeholder = placeholder;
        this._content = content;
        this._placeholderIsUpdated = false;
    }

    // Getter方法
    public String paramName() {
        return _paramName;
    }

    public String placeholder() {
        return _placeholder;
    }

    public String content() {
        return _content;
    }

    private static final Pattern _regex = Pattern.compile("'__@[a-zA-Z0-9_]+'\\s*=\\s*'__@[a-zA-Z0-9_]+'\\s*AND\\s*\\((.+)\\)", Pattern.CASE_INSENSITIVE);

    // 更新占位符方法
    public void tryUpdate(String commandText) {
        if (this._placeholderIsUpdated) return;

        Matcher matcher = _regex.matcher(commandText);
        if (matcher.find()) {
            this._placeholder = matcher.group(0);
            this._content = matcher.group(1);
        }
        this._placeholderIsUpdated = true;
    }
}
