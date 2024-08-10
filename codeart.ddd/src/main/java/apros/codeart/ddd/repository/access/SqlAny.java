package apros.codeart.ddd.repository.access;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlAny {
    private final int _index;
    private final String _paramName;
    private String _placeholder;
    private String _content;
    private boolean _placeholderIsUpdated;
    private final Pattern _regex;

    // 全部参数的构造函数
    public SqlAny(int index, String paramName, String placeholder, String content) {
        this._index = index; //在any条件里的序号
        this._paramName = paramName;
        this._placeholder = placeholder;
        this._content = content;
        this._placeholderIsUpdated = false;
        this._regex = Pattern.compile(String.format("'__@[a-zA-Z0-9_]+_any_%s'\\s*=\\s*'__@[a-zA-Z0-9_]+_any_%s'\\s*AND\\s*\\((.+?)\\)", this._index, this._index), Pattern.CASE_INSENSITIVE);
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
