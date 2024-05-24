package apros.codeart.ddd;

import apros.codeart.i18n.Language;

public class DataVersionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7626255381171289711L;

	public DataVersionException(Class<?> objectType, Object id) {
		super(Language.strings("apros.codeart.ddd", "DataVersionError", objectType.getName(), id));
	}
}
