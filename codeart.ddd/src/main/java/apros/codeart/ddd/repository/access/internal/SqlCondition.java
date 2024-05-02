package apros.codeart.ddd.repository.access.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apros.codeart.ddd.MapData;
import apros.codeart.ddd.repository.access.SqlAny;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;
import apros.codeart.util.StringUtil;

public final class SqlCondition {

	private ArrayList<SqlLike> _likes;

	public Iterable<SqlLike> likes() {
		return _likes;
	}

	private ArrayList<SqlIn> _ins;

	public Iterable<SqlIn> ins() {
		return _ins;
	}

	private ArrayList<SqlAny> _anys;

	public Iterable<SqlAny> anys() {
		return _anys;
	}

	private String _code;

	public String code() {
		return _code;
	}

	private String _probeCode;

	/**
	 * 探测代码，我们增加了很多自定义语法，这些语法sql引擎不能识别，
	 * 
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
		return _code.length() == 0;
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
			var g = matcher.group(1);
			int length = g.length();

			var newExp = StringUtil.trim(g);
			boolean before = newExp.startsWith("%");
			boolean after = newExp.endsWith("%");
			newExp = StringUtil.trim(newExp, "%");
			var para = StringUtil.substr(newExp, 1);
			newExp = String.format(" %s", newExp);// 补充一个空格

			SqlLike like = new SqlLike(para, before, after);
			_likes.add(like);

			var index = matcher.start(1);

			code = StringUtil.insert(code, index + offset, newExp);
			code = StringUtil.remove(code, index + offset + newExp.length(), length);

			offset += newExp.length() - length; // 记录偏移量
		}

		return code;
	}

	/// <summary>
	/// 解析in写法 id in @ids 转为可以执行的语句
	/// </summary>
	/// <param name="code"></param>
	/// <returns></returns>
	private String parseIn(String code) {

		Matcher matcher = _inRegex.matcher(code);
		int offset = 0;
		while (matcher.find()) {
			var g = matcher.group(0);
			int length = g.length();

			var field = matcher.group(1);

			var newExp = StringUtil.trim(matcher.group(2));
			var para = StringUtil.substr(newExp, 1);
			newExp = String.format("%s in (@%s)", field, para);

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
		while (matcher.find()) {
			var placeholder = matcher.group(0);
			var paraName = matcher.group(1);
			var content = matcher.group(2);
			// 转义
//            content = content.Replace("&lt;","<").Replace("&gt;", ">");

			SqlAny any = new SqlAny(paraName, placeholder, content);
			_anys.add(any);

			probeCode = probeCode.replace(placeholder, content);
		}

		_probeCode = probeCode;

		return code;
	}

	@Override
	public String toString() {
		return this.code();
	}

	String process(String commandText, MapData param) {
		if (this.isEmpty())
			return commandText;

		// 先处理any
		for (var any : _anys) {
			if (param.containsKey(any.paramName()))
				commandText = commandText.replace(any.placeholder(), any.content());
			else
				commandText = commandText.replace(any.placeholder(), "0=0"); // 没有参数表示永远为真，这里不能替换为空文本，因为会出现where
																				// 没有条件的BUG，所以为 where 0=0
		}

		for (var like : _likes) {
			var name = like.paramName();
			var value = TypeUtil.as(param.get(name), String.class);
			if (value == null)
				continue; // 因为有any语法，所以没有传递参数也正常

			if (like.after() && like.before())
				param.put(name, MessageFormat.format("%{0}%", value));
			else if (like.after())
				param.put(name, MessageFormat.format("{0}%", value));
			else if (like.before())
				param.put(name, MessageFormat.format("%{0}", value));
		}

		for (var sin : _ins) {
			var name = sin.paramName();
			var values = TypeUtil.as(param.get(name), Iterable.class);
			param.remove(name);

			if (ListUtil.exists(values)) {
				var code = new StringBuilder();
				int index = 0;
				StringUtil.appendFormat(code, "%s in (", sin.field());
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

	private static Pattern _likeRegex = Pattern.compile("[ ]+?like([ %]+?@[%\\d\\w0-9]+)", Pattern.CASE_INSENSITIVE);

	private static Pattern _inRegex = Pattern.compile("([\\d\\w0-9]+?)[ ]+?in[ ]+?(@[\\d\\w0-9]+)",
			Pattern.CASE_INSENSITIVE);

	private static Pattern _anyRegex = Pattern.compile("@([^\\< ]+)\\<([^\\>]+)\\>", Pattern.CASE_INSENSITIVE);

	public static final SqlCondition Empty = new SqlCondition(StringUtil.empty());

}
