package apros.codeart.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.base.CaseFormat;

import apros.codeart.io.IOBuffer;
import apros.codeart.pooling.PoolingException;

public final class StringUtil {
	private StringUtil() {
	};

	public static void clear(StringBuilder str) {
		str.setLength(0);
	}

	public static void appendFormat(StringBuilder str, String format, Object... args) {
		str.append(String.format(format, args));
	}

	public static void appendMessageFormat(StringBuilder str, String format, Object... args) {
		str.append(MessageFormat.format(format, args));
	}

	/**
	 * 向字符串末尾添加一个换行符
	 * 
	 * @param str
	 */
	public static void appendLine(StringBuilder str) {
		str.append(System.lineSeparator());
	}

	public static void appendLine(StringBuilder str, String content) {
		str.append(content);
		str.append(System.lineSeparator());
	}

	public static void removeLast(StringBuilder str) {
		str.deleteCharAt(str.length() - 1);
	}

	public static char last(StringBuilder str) {
		return str.charAt(str.length() - 1);
	}

	public static void replaceAll(StringBuilder sb, String from, String to) {
		int index = sb.indexOf(from);
		while (index != -1) {
			sb.replace(index, index + from.length(), to);
			index += to.length();
			index = sb.indexOf(from, index);
		}
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.equals("");
	}

	public static String empty() {
		return "";
	}

	public static char charEmpty() {
		return '\u0000';
	}

	public static boolean isAscii(String str) {
		if (str == null) {
			return false;
		}

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) > 127) {
				return false;
			}
		}

		return true;
	}

	public static String substr(String source, int startIndex, int length) {
		int endIndex = startIndex + length;
		return source.substring(startIndex, endIndex);
	}

	public static String substr(String source, int startIndex) {
		return source.substring(startIndex);
	}

	public static String trimStart(String str, String trimValue) {
		while (str.indexOf(trimValue) == 0) {
			str = substr(str, trimValue.length());
		}
		return str;
	}

	public static String trimEnd(String str, String trimValue) {
		int p = str.lastIndexOf(trimValue);

		while (p != -1 && p == (str.length() - trimValue.length())) {
			str = substr(str, 0, p);
			p = str.lastIndexOf(trimValue);
		}
		return str;
	}

	public static String trim(String str, String trimValue) {
		str = trimStart(str, trimValue);
		return trimEnd(str, trimValue);
	}

	public static String trim(String str) {
		return trim(str, " ");
	}

	public static String insert(String str, int offset, String value) {
		var sb = new StringBuilder();
		sb.insert(offset, value);
		return sb.toString();
	}

	public static String remove(String original, int index, int length) {
		if (index < 0 || length < 0 || index > original.length()) {
			throw new IllegalArgumentException("Index or length out of bounds");
		}

		// 计算删除部分后面段的起始索引
		int end = index + length;

		// 检查结束索引是否超出字符串长度
		if (end > original.length()) {
			end = original.length();
		}

		// 创建新字符串，由前一部分和后一部分组成
		return original.substring(0, index) + original.substring(end);
	}

	public static ArrayList<String> trim(String[] strs) {
		return ListUtil.<String, String>map(strs, (e) -> StringUtil.trim(e));
	}

	public static ArrayList<String> trim(Iterable<String> strs) {
		return ListUtil.<String, String>map(strs, (e) -> StringUtil.trim(e));
	}

	/**
	 * 组成一句话，可以指定分隔符
	 * 
	 * @param separator
	 * @param items
	 * @return
	 * @throws Exception
	 */
	public static String join(String separator, Iterable<String> items) {
		var sb = new StringBuilder();
		var i = 0;
		for (var item : items) {
			if (i > 0)
				sb.append(separator);
			sb.append(item);
			i++;
		}
		return sb.toString();
	}

	/**
	 * 组成一句话，可以指定分隔符
	 * 
	 * @param <T>
	 * @param separator
	 * @param items
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static <T> String join(String separator, Iterable<T> items, Function<T, String> map) {
		var sb = new StringBuilder();
		var i = 0;
		for (var item : items) {
			if (i > 0)
				sb.append(separator);
			sb.append(map.apply(item));
			i++;
		}
		return sb.toString();
	}

	/**
	 * 转换成多行
	 * 
	 * @param items
	 * @return
	 * @throws PoolingException
	 */
	public static String lines(Iterable<String> items) {
		return join(System.lineSeparator(), items);
	}

	public static <T> String lines(Iterable<T> items, Function<T, String> map) {
		return join(System.lineSeparator(), items, map);
	}

	public static String firstToUpper(String str) {
		return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, str);
	}

	public static boolean startsWithIgnoreCase(String str, String prefix) {
		var start = substr(str, prefix.length());
		return start.toLowerCase().startsWith(prefix.toLowerCase());
	}

	public static boolean containsIgnoreCase(Iterable<String> list, String target) {
		for (String element : list) {
			if (element.equalsIgnoreCase(target)) {
				return true;
			}
		}
		return false;
	}

	public static boolean contains(Iterable<String> list, String target) {
		for (String element : list) {
			if (element.equals(target)) {
				return true;
			}
		}
		return false;
	}

	public static Iterable<String> distinctIgnoreCase(Iterable<String> list) {
		// 创建一个不区分大小写的 TreeSet，然后使用 Stream API 过滤重复项
		Set<String> set = StreamSupport.stream(list.spliterator(), false)
				.collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));

		return set;
	}

	public static int indexOfIgnoreCase(String source, String target) {
		Pattern pattern = Pattern.compile(Pattern.quote(target), Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(source);

		return matcher.find() ? matcher.start() : -1;
	}

	/**
	 * 
	 * 用utf-8编码，预估字符串最大可能占有的字节数
	 * 
	 * 虽然 maxBytesPerChar() 提供了一个快速且简便的方法来估算可能的最大字节长度，但它往往不适合用于精确的内存分配或性能优化，
	 * 
	 * 因为它可能导致显著的内存浪费。在性能和内存使用敏感的应用中，应考虑实际编码字符串来精确计算所需的字节长度。
	 * 
	 * @param value
	 * @return
	 */
	public static int maxBytesPerChar(String value, Charset charset) {

		CharsetEncoder encoder = charset.newEncoder();

		// 获取每个字符可能占用的最大字节数
		float maxBytesPerChar = encoder.maxBytesPerChar();
		int maxByteLength = (int) (value.length() * maxBytesPerChar);

		return maxByteLength;
	}

	public static int maxBytesPerChar(String value) {
		return maxBytesPerChar(value, StandardCharsets.UTF_8);
	}

	public static int getBytesSize(String value) {
		return getBytesSize(value, StandardCharsets.UTF_8);
	}

	/**
	 * 
	 * 获得字符串实际占用的字节数
	 * 
	 * @param value
	 * @param charset
	 * @return
	 */
	public static int getBytesSize(String value, Charset charset) {
		var max = maxBytesPerChar(value, charset);
		try (var temp = IOBuffer.borrow(max)) {
			ByteBuffer buffer = temp.getItem();
			int start = 0;
			CharsetEncoder encoder = charset.newEncoder();
			encoder.encode(CharBuffer.wrap(value), buffer, true);
			int end = buffer.position();
			return end - start;
		}
	}

}