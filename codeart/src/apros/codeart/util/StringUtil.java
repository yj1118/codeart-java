package apros.codeart.util;

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

}