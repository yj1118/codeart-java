package com.apros.codeart.util;

import static com.apros.codeart.i18n.Language.strings;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public final class ArgumentAssert {

	private ArgumentAssert() {
	}

	public static void isNotNullOrEmpty(String value, String parameterName) {
		// 检查字符串不为null
		Preconditions.checkNotNull(value, strings("codeart", "ArgCanNotNull", parameterName));

		// 检查字符串不是空字符串
		Preconditions.checkArgument(!value.isEmpty(), strings("codeart", "CannotEmpty", parameterName));
	}

	public static void isNotNullOrEmpty(Iterable<?> value, String parameterName) {
		// 检查字符串不为null
		Preconditions.checkNotNull(value, strings("codeart", "ArgCanNotNull", parameterName));

		// 检查字符串不是空字符串
		Preconditions.checkArgument(!Iterables.isEmpty(value), strings("codeart", "CannotEmpty", parameterName));
	}

	public static void isNotNullOrEmpty(String[] value, String parameterName) {
		// 检查字符串不为null
		Preconditions.checkNotNull(value, strings("codeart", "ArgCanNotNull", parameterName));

		// 检查字符串不是空字符串
		Preconditions.checkArgument(value.length > 0, strings("codeart", "CannotEmpty", parameterName));
	}

}
