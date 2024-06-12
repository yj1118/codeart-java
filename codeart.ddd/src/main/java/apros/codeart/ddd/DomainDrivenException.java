package apros.codeart.ddd;

import java.io.Serial;

public class DomainDrivenException extends RuntimeException {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public DomainDrivenException(String message) {
        super(message);
    }
}
