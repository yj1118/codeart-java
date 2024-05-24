package apros.codeart.util;

import apros.codeart.i18n.Language;

public class TypeMismatchException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8325185542776565809L;

	public TypeMismatchException(Class<?> expectedType, Class<?> actualType) {
		super(Language.strings("apros.codeart", "TypeMismatchFor", expectedType.getName(), actualType.getName()));
	}

}
