package apros.codeart.ddd.repository.access.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.repository.access.SqlAny;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public final class SqlCondition {

    private final ArrayList<SqlLike> _likes;

    public Iterable<SqlLike> likes() {
        return _likes;
    }

    private final ArrayList<SqlIn> _ins;

    public Iterable<SqlIn> ins() {
        return _ins;
    }

    private final ArrayList<SqlAny> _anys;

    public Iterable<SqlAny> anys() {
        return _anys;
    }

    private final String _code;

    public String code() {
        return _code;
    }

    private String _probeCode;

    /**
     * 探测代码，我们增加了很多自定义语法，这些语法sql引擎不能识别，
     * <p>
     * 在探测查询列时，我们需要用sql引擎可以识别的语法，所以需要探测代码，该代码是sql可以识别的
     *
     * @return
     */
    public String probeCode() {
        return _probeCode;
    }

    public SqlCondition(String code) {
        _likes = new ArrayList<SqlLike>();
        _ins = new ArrayList<SqlIn>();
        _anys = new ArrayList<SqlAny>();
        _code = parse(code);
    }

    public boolean isEmpty() {
        return _code.isEmpty();
    }

    private String parse(String code) {
        if (StringUtil.isNullOrEmpty(code))
            return code;
        code = parseLike(code);
        code = parseIn(code);
        code = parseAny(code);
        return code;
    }

    private String parseLike(String code) {

        // 创建 matcher 对象
        Matcher matcher = _likeRegex.matcher(code);
        int offset = 0;

        // 检查文本中的所有匹配项
        while (matcher.find()) {
            var fieldName = matcher.group(1);
            var g = matcher.group(2);
            int length = g.length();

            var newExp = StringUtil.trim(g);
            boolean before = newExp.startsWith("%");
            boolean after = newExp.endsWith("%");
            newExp = StringUtil.trim(newExp, "%");
            var para = StringUtil.substr(newExp, 1);
            newExp = String.format(" %s", newExp);// 补充一个空格

            SqlLike like = new SqlLike(fieldName, para, before, after);
            _likes.add(like);

            var index = matcher.start(2);

            code = StringUtil.insert(code, index + offset, newExp);
            code = StringUtil.remove(code, index + offset + newExp.length(), length);

            offset += newExp.length() - length; // 记录偏移量
        }

        return code;
    }

    /**
     * 解析in写法 id in @ids 转为可以执行的语句
     *
     * @param code
     * @return
     */
    private String parseIn(String code) {

        Matcher matcher = _inRegex.matcher(code);
        int offset = 0;
        while (matcher.find()) {
            var g = matcher.group(0);
            int length = g.length();

            var field = matcher.group(1);

            var newExp = StringUtil.trim(matcher.group(2));
            var para = newExp;
            newExp = String.format("%s IN (@%s)", field, para);

            SqlIn sin = new SqlIn(field, para, newExp);
            _ins.add(sin);

            var index = matcher.start(0);

            code = StringUtil.insert(code, index + offset, newExp);
            code = StringUtil.remove(code, index + offset + newExp.length(), length);

            offset += newExp.length() - length; // 记录偏移量
        }
        return code;
    }

    /// <summary>
    /// 解析任意条件的写法 @name{name like @name} 转为可以执行的语句
    /// </summary>
    /// <param name="code"></param>
    /// <returns></returns>
    private String parseAny(String code) {
        var probeCode = code;
        Matcher matcher = _anyRegex.matcher(code);

        int offset = 0;

        while (matcher.find()) {
            var placeholder = matcher.group(0);
            var paraName = matcher.group(1);
            var content = matcher.group(2);

            // 转义
//            content = content.Replace("&lt;","<").Replace("&gt;", ">");

            probeCode = probeCode.replace(placeholder, content);

            String replaceStr = String.format("'__@%s_any_%s'='__@%s_any_%s' AND (%s)", paraName, _anys.size(), paraName, _anys.size(), content);

            var index = matcher.start();
            int length = matcher.end() - index; // 计算匹配的字符串长度

            code = StringUtil.insert(code, index + offset, replaceStr);
            code = StringUtil.remove(code, index + offset + replaceStr.length(), length);

            SqlAny any = new SqlAny(_anys.size(), paraName, replaceStr, content);
            _anys.add(any);

            offset += replaceStr.length() - length; // 记录偏移量
        }

        _probeCode = probeCode;

        return code;
    }

    @Override
    public String toString() {
        return this.code();
    }

    String process(String commandText, MapData param) {
        if (this.isEmpty() || param == null)
            return commandText;

        // 先处理any
        for (var any : _anys) {
            // 经过sqlParser转移后，占位符可能会变，需要更新下，这样后续操作效率高
            any.tryUpdate(commandText);

            if (param.containsKey(any.paramName()))
                commandText = commandText.replace(any.placeholder(), any.content());
            else
                commandText = commandText.replace(any.placeholder(), "1=1"); // 没有参数表示永远为真，这里不能替换为空文本，因为会出现where
            // 没有条件的BUG，所以为 where 0=0
        }

        for (var like : _likes) {
            var name = like.paramName();
            var value = TypeUtil.as(param.get(name), String.class);
            if (value == null)
                continue; // 因为有any语法，所以没有传递参数也正常

            if (like.after() && like.before())
                param.put(name, StringUtil.format("%{0}%", value));
            else if (like.after())
                param.put(name, StringUtil.format("{0}%", value));
            else if (like.before())
                param.put(name, StringUtil.format("%{0}", value));
        }

        if (!_ins.isEmpty()) {
            commandText = processIn(commandText, param);
        }

        return commandText;
    }

    private String processIn(String commandText, MapData param) {
        // 针对最终生成的sql，收集一次ins，然后再替换，可以解决程序员写的表达式与生成后的表达式不匹配的问题

        var ins = _getIns.apply(commandText);
        for (var sin : ins) {
            var name = sin.paramName();
            var values = TypeUtil.as(param.get(name), Iterable.class);
            param.remove(name);

            if (ListUtil.exists(values)) {
                var code = new StringBuilder();
                int index = 0;
                StringUtil.appendFormat(code, "%s IN (", sin.field());
                for (var value : values) {
                    var valueName = String.format("%s%s", name, index);
                    param.put(valueName, value);
                    StringUtil.appendFormat(code, "@%s,", valueName);
                    index++;
                }
                if (code.length() > 1)
                    StringUtil.removeLast(code);
                code.append(")");

                commandText = commandText.replace(sin.placeholder(), code.toString());
            } else {
                commandText = commandText.replace(sin.placeholder(), "0=1"); // 在in语句中，如果 id in
                // ()，没有任何匹配的数值条件，那么就是无匹配结果，所以0=1
            }
        }
        return commandText;
    }

    //String regex = "\\s*(@\\w+)\\{([^{}]*)\\}";

    private static final Pattern _likeRegex = Pattern.compile("([^\\s{]+?)\\s+like([ %]+?@[%\\w0-9]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern _inRegex = Pattern.compile("(\\S+)\\s+IN\\s+\\(?\\s*@([^\\s)]+)\\s*\\)?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern _anyRegex = Pattern.compile("@([^{ ]+)\\{([^}]+)}", Pattern.CASE_INSENSITIVE);

    public static final SqlCondition Empty = new SqlCondition(StringUtil.empty());

    private static final Function<String, Iterable<SqlIn>> _getIns = LazyIndexer.init((commandText) -> {
        ArrayList<SqlIn> ins = new ArrayList<>();

        Matcher matcher = _inRegex.matcher(commandText);
        int offset = 0;
        while (matcher.find()) {
            var g = matcher.group(0);
            int length = g.length();

            var field = matcher.group(1);

            var newExp = StringUtil.trim(matcher.group(2));
            var para = newExp;
            newExp = String.format("%s IN (@%s)", field, para);

            SqlIn sin = new SqlIn(field, para, newExp);
            ins.add(sin);
        }

        return ins;
    });


}
