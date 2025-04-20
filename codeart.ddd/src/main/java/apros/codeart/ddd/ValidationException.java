package apros.codeart.ddd;

import java.io.Serial;

public class ValidationException extends BusinessException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -7233981740857887941L;
    private final ValidationResult _result;

    public ValidationResult result() {
        return _result;
    }

    public ValidationException(ValidationResult result) {
        super(result.getMessage());
        _result = result;
    }
}
