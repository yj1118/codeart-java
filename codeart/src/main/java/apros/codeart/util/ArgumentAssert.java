package apros.codeart.util;

import static apros.codeart.i18n.Language.strings;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public final class ArgumentAssert {

	private ArgumentAssert() {
	}

	public static void isNotNullOrEmpty(String value, String parameterName) {
		// 检查字符串不为null
		Preconditions.checkNotNull(value, strings("apros.codeart", "ArgCanNotNull", parameterName));

		// 检查字符串不是空字符串
		Preconditions.checkArgument(!value.isEmpty(), strings("apros.codeart", "CannotEmpty", parameterName));
	}

	public static void isNotNullOrEmpty(Iterable<?> value, String parameterName) {
		// 检查字符串不为null
		Preconditions.checkNotNull(value, strings("apros.codeart", "ArgCanNotNull", parameterName));

		// 检查字符串不是空字符串
		Preconditions.checkArgument(!Iterables.isEmpty(value), strings("apros.codeart", "CannotEmpty", parameterName));
	}

	public static void isNotNullOrEmpty(String[] value, String parameterName) {
		// 检查字符串不为null
		Preconditions.checkNotNull(value, strings("apros.codeart", "ArgCanNotNull", parameterName));

		// 检查字符串不是空字符串
		Preconditions.checkArgument(value.length > 0, strings("apros.codeart", "CannotEmpty", parameterName));
	}

	public static void isNotNull(Object value, String parameterName) {
		// 检查不为null
		Preconditions.checkNotNull(value, strings("apros.codeart", "ArgCanNotNull", parameterName));
	}

	/**
	 * 不能小于或等于0
	 * 
	 * @param value
	 * @param parameterName
	 */
	public static void lessThanOrEqualZero(int value, String parameterName) {
		if (value <= 0)
			throw new IllegalArgumentException(strings("apros.codeart", "LessThanOrEqualZero", parameterName));
	}

}
