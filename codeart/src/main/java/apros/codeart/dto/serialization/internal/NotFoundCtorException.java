package apros.codeart.dto.serialization.internal;

import apros.codeart.i18n.Language;

public class NotFoundCtorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5850255317443578578L;

	public NotFoundCtorException(Class<?> objectType) {
		super(Language.strings("codeart", "DTONotFoundCtor", objectType.getSimpleName()));
	}

}
