package apros.codeart;

import apros.codeart.i18n.Language;

public class NoTypeDefinedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3155673696123721561L;

	public NoTypeDefinedException(Class<?> type) {
		super(Language.strings("codeart", "NoTypeDefined", type.getName()));
	}

	public NoTypeDefinedException(String typeName) {
		super(Language.strings("codeart", "NoTypeDefined", typeName));
	}

}
