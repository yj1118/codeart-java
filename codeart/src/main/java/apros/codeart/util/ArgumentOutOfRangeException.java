package apros.codeart.util;

import static apros.codeart.i18n.Language.strings;

public class ArgumentOutOfRangeException extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6509558479008386759L;

	public ArgumentOutOfRangeException(String paramName) {
		super(strings("apros.codeart", "ArgOutOfRange", paramName));
	}
}