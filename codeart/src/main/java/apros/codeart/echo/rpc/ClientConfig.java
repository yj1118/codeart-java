package apros.codeart.echo.rpc;

public class ClientConfig {

    private final int _timeout;

    /**
     * 请求超时时间
     *
     * @return
     */
    public int timeout() {
        return _timeout;
    }

    private ClientConfig(int timeout) {
        _timeout = timeout;
    }

    public static final ClientConfig Instance = new ClientConfig(EchoRPC.clientTimeout());
}
