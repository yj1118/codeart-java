package apros.codeart.dto.serialization.internal;

import apros.codeart.i18n.Language;

import java.io.Serial;

public class NotFoundCtorException extends RuntimeException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 5850255317443578578L;

    public NotFoundCtorException(Class<?> objectType) {
        super(Language.strings("apros.codeart", "DTONotFoundCtor", objectType.getSimpleName()));
    }

}
