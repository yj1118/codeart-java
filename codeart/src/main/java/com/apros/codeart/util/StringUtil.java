package com.apros.codeart.util;

import java.util.UUID;
import java.util.function.Function;

import com.apros.codeart.pooling.PoolingException;

public final class StringUtil {
	private StringUtil() {
	};

	public static void clear(StringBuilder str) {
		str.setLength(0);
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

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.equals("");
	}

	public static String empty() {
		return "";
	}

	public static char charEmpty() {
		return '\u0000';
	}

	public static String substr(String source, int startIndex, int length) {
		int endIndex = startIndex + length;
		return source.substring(startIndex, endIndex);
	}

	public static String substr(String source, int startIndex) {
		return source.substring(startIndex, startIndex);
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

	/**
	 * 获得不带分隔符的guid
	 * 
	 * @return
	 */
	public static String uuid() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "");
	}

}