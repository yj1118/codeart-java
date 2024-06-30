package apros.codeart.ddd.repository;

import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.i18n.Language;

import java.io.Serial;

public class RepeatedCommitException extends DomainDrivenException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -7366068880543324255L;

    public RepeatedCommitException() {
        super(Language.strings("apros.codeart.ddd", "RepeatedCommit"));
    }
}
