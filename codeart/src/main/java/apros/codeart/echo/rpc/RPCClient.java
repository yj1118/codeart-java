package apros.codeart.echo.rpc;

import java.util.function.Consumer;

import apros.codeart.dto.DTObject;
import apros.codeart.pooling.Pool;
import apros.codeart.pooling.PoolConfig;

public class RPCClient {

    public static DTObject invoke(String method, Consumer<DTObject> fillArg) {
        var arg = DTObject.editable();
        fillArg.accept(arg);
        return invoke(method, arg);
    }

    public static DTObject invoke(String method, DTObject arg) {
        return _pool.using((client) -> {
            return client.invoke(method, arg);
        });
    }

    public static DTObject invoke(String method) {
        return invoke(method, DTObject.editable());
    }

//	 #region 获取客户端实例

    private static final Pool<IClient> _pool = new Pool<IClient>(IClient.class, new PoolConfig(10, 500, 60), (isTempItem) -> {
        return RPCClientFactory.get().create(ClientConfig.Instance);
    }, (client) -> {
        client.clear();
    });

//	#endregion

    /**
     * 释放客户端资源
     */
    static void cleanup() {
        _pool.dispose();
    }

}
