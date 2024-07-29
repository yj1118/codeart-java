package apros.codeart.echo.rpc;

public final class RPCServerFactory {
    private RPCServerFactory() {
    }

    private static IServerFactory _factory;

    public static IServerFactory get() {
        return _factory;
    }

    /**
     * 注册服务工厂
     */
    public static void register(IServerFactory factory) {
        _factory = factory;
    }

}
