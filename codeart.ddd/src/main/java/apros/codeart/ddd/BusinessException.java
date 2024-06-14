package apros.codeart.ddd;

import apros.codeart.UIException;

public class BusinessException extends UIException {


    public BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Exception innerException) {
        super(message, innerException);
    }

}
