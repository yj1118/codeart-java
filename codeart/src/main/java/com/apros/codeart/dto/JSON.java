package com.apros.codeart.dto;

import static com.apros.codeart.runtime.TypeUtil.as;
import static com.apros.codeart.runtime.TypeUtil.is;
import static com.apros.codeart.util.StringUtil.last;
import static com.apros.codeart.util.StringUtil.removeLast;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;

import com.apros.codeart.runtime.FieldUtil;
import com.apros.codeart.util.StringUtil;
import com.apros.codeart.util.TimeUtil;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

class JSON {
	private JSON() {
	}

	public static String getCode(Object value) {
		var sb = new StringBuilder();
		writeValue(sb, value);
		return sb.toString();
	}

	public static void writeValue(StringBuilder sb, Object value) {
		if (value == null) {
			sb.append("null");
			return;
		}

		var valueClass = value.getClass();

		if (valueClass == String.class) {
			writeString(sb, value.toString());
			return;
		}

		if (valueClass == long.class || valueClass == int.class || valueClass == float.class || valueClass == byte.class
				|| valueClass == double.class || valueClass == short.class) {
			sb.append(value);
			return;
		}

		if (valueClass == Boolean.class) {
			sb.append(value.toString().toLowerCase());
			return;
		}

		if (valueClass.isEnum()) {
			Enum<?> e = (Enum<?>) value;
			int ordinalValue = e.ordinal();
			sb.append(ordinalValue);
			return;
		}

		if (valueClass == Instant.class) {
			var t = TimeUtil.toUTC((Instant) value);
			writeInstant(sb, t);
			return;
		}

		if (valueClass == LocalDateTime.class) {
			var t = TimeUtil.toUTC((LocalDateTime) value);
			writeInstant(sb, t);
			return;
		}

		if (is(valueClass, Map.class)) {
			writeMap(sb, (Map<?, ?>) value);
			return;
		}

		if (is(valueClass, Iterable.class)) {
			writeIterable(sb, (Iterable<?>) value);
			return;
		}

		else {
			writeObject(sb, value);
		}
	}

	private static void writeInstant(StringBuilder sb, Instant value) {
		sb.append("new Date(\"");
		sb.append(TimeUtil.formatMemoized(value, "yyyy-MM-ddTHH:mm:ss.fffZ")); // 只有转为utc时间，火狐等浏览器才识别
		sb.append("\")");
	}

	// 这主要是将带换行符的json代码，每行末尾添加了\字符，这样js引擎才能识别带换行的字符串
	public static void writeString(StringBuilder sb, String value) {

		sb.append("\"");
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '\"':
				sb.append("\\\"");
				break;
			case '\\': {
				sb.append("\\\\");
				break;
			}
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				sb.append(c);
				break;
			}
		}
		sb.append("\"");
	}

	private static void writeMap(StringBuilder sb, Map<?, ?> value) {
		boolean hasItems = false;
		sb.append("{");
		for (Map.Entry<?, ?> entry : value.entrySet()) {
			writeValue(sb, entry.getKey());
			sb.append(":");
			writeValue(sb, entry.getValue());
			sb.append(",");
			if (hasItems == false)
				hasItems = true;
		}
		if (hasItems)
			removeLast(sb);
		sb.append("}");
	}

	private static void writeIterable(StringBuilder sb, Iterable<?> value) {
		boolean hasItems = false;
		sb.append("[");
		for (var item : value) {
			writeValue(sb, item);
			sb.append(",");
			if (hasItems == false)
				hasItems = true;
		}

		if (hasItems)
			removeLast(sb);
		sb.append("]");
	}

	private static void writeObject(StringBuilder sb, Object value) {
		var dto = as(value, DTObject.class);
		if (dto != null) {
			dto.fillCode(sb, false, true);
			return;
		}

		sb.append("{");

		FieldUtil.eachMemoized(value, (n, v) -> {
			sb.append(n);
			sb.append(":");
			writeValue(sb, v);
			sb.append(",");
		});

		if (last(sb) == ',')
			removeLast(sb);
		sb.append("}");

	}

	public static String readString(String value) {
		if (StringUtil.isNullOrEmpty(value))
			return StringUtil.empty();

		var sb = new StringBuilder();
		char sign = StringUtil.charEmpty();
		for (var pos = 0; pos < value.length(); pos++) {
			var c = value.charAt(pos);
			switch (c) {
			case '\'':
			case '\"': {
				if (sign == StringUtil.charEmpty()) {
					sign = c;
				} else {
					if (sign == c) {
						sign = StringUtil.charEmpty();
					} else {
						sb.append(c);
					}
				}
			}
				break;
			case '\\': {
				if (pos == value.length() - 1) {
					// 最后个字符
					sb.append(c);
				} else {
					var next = value.charAt(pos + 1);

					switch (next) {
					case '\"': {
						sb.append('\"');
						pos++;
					}
						break;
					case '\\': {
						sb.append('\\');
						pos++;
					}
						break;
					case 'b': {
						sb.append('\b');
						pos++;
					}
						break;
					case 'f': {
						sb.append('\f');
						pos++;
					}
						break;
					case 'n': {
						sb.append('\n');
						pos++;
					}
						break;
					case 'r': {
						sb.append('\r');
						pos++;
					}
						break;
					case 't': {
						sb.append('\t');
						pos++;
					}
						break;
					}
				}

				break;
			}
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	public static Object getValueByString(String code) {
		var value = JSON.readString(code);
		var time = JSON.parseInstant(value);
		if (time != null)
			return time; // 有可能是客户端的JS库的JSON.Parse处理后得到的时间，得特别处理
		return value;
	}

	public static Object getValueByNotString(String code) {
		if (code == null || code.equals("null"))
			return null;
		else if (code.equalsIgnoreCase("true"))
			return true;
		else if (code.equalsIgnoreCase("false"))
			return false;
		else {
			var intValue = Ints.tryParse(code);
			if (intValue != null)
				return intValue;

			var longValue = Longs.tryParse(code);
			if (longValue != null)
				return longValue;

			var floatValue = Floats.tryParse(code);

			if (floatValue != null) {
				var dot = code.indexOf(".");
				if (dot == -1 || (code.length() - dot - 1) <= 6) {
					// 6位及以内的小数采用float，因为超过6位，输出就不准确了，被截获了，float转string
					return floatValue;
				}
			}

			var doubleValue = Doubles.tryParse(code);
			if (doubleValue != null)
				return doubleValue;
		}
		return code;
	}

	private final static Pattern _date = Pattern.compile("new Date\\(\"(.+?)\"\\)", Pattern.CASE_INSENSITIVE);

	// Pattern是线程安全的
	private final static Pattern _date2 = Pattern
			.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})Z", Pattern.CASE_INSENSITIVE);

	public static Instant parseInstant(String code) {
		if (_date2.matcher(code).matches()) {
			return Instant.parse(code);
		}

		var matcher = _date.matcher(code);
		if (matcher.matches()) {
			code = matcher.group(1);
			return Instant.parse(code);
		}

		return null;
	}

}
