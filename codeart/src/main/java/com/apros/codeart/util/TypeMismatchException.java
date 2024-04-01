package com.apros.codeart.util;

import com.apros.codeart.i18n.Language;

public class TypeMismatchException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8325185542776565809L;

	public TypeMismatchException(Class<?> expectedType, Class<?> actualType) {
		super(Language.strings("codeart", "TypeMismatchFor", expectedType.getName(), actualType.getName()));
	}

}
