package apros.codeart.util;

import apros.codeart.i18n.Language;

public class TypeUnsafeAccessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4166347031194214026L;

	public TypeUnsafeAccessException(Class<?> type) {
		super(Language.strings("apros.codeart", "TypeUnsafeConcurrentAccess", type.getName()));
	}

}
