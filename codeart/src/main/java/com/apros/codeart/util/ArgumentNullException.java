package com.apros.codeart.util;

import static com.apros.codeart.i18n.Language.strings;

public class ArgumentNullException extends IllegalArgumentException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3659660620867029451L;

	public ArgumentNullException(String paramName) {
		super(strings("ArgCanNotNull", paramName));
	}
}