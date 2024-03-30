package com.apros.codeart.util;

import com.apros.codeart.i18n.Language;

public class TypeUnsafeAccessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4166347031194214026L;

	public TypeUnsafeAccessException(Class<?> type) {
		super(Language.strings("TypeUnsafeConcurrentAccess", type.getName()));
	}

}
