package com.apros.codeart.util;

import static com.apros.codeart.i18n.Language.strings;

import com.google.common.base.Preconditions;

public final class ArgumentAssert {

	private ArgumentAssert() {
	}

	public static void isNotNullOrEmpty(String value, String parameterName) {
		// 检查字符串不为null
		Preconditions.checkNotNull(value, strings("ArgCanNotNull", parameterName));

		// 检查字符串不是空字符串
		Preconditions.checkArgument(!value.isEmpty(), strings("CannotEmpty", parameterName));
	}

}
