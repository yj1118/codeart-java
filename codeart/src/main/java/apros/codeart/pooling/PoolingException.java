package apros.codeart.pooling;

import java.io.Serial;

public class PoolingException extends RuntimeException {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 6500211809452215882L;

    // 构造函数
    public PoolingException() {
        super();
    }

    // 带有消息的构造函数
    public PoolingException(Throwable innerException) {
        super("", innerException);
    }

    public PoolingException(String message, Throwable innerException) {
        super(message, innerException);
    }

    public PoolingException(String message) {
        super(message);
    }
}