package apros.codeart;

import java.io.Serial;

public class UIException extends RuntimeException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -4503168230778998420L;

    public UIException() {
    }

    public UIException(String message) {
        super(message);
    }

    public UIException(String message, Exception innerException) {
        super(message, innerException);
    }

    @Override
    public String toString() {
        // 通过重写后，确保抛出的异常不会有 BusinessException.xxx:message 的信息
        // this.getMessage(); 只会有原始的信息
        return this.getMessage();
    }

    public static boolean is(Exception ex) {
        return UIException.class.isAssignableFrom(ex.getClass());
    }

}
