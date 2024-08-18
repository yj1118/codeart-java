package apros.codeart.rabbitmq.rpc;

import java.util.function.Function;

import apros.codeart.echo.rpc.EchoRPC;
import apros.codeart.rabbitmq.ConsumerConfig;
import apros.codeart.rabbitmq.MQConnConfig;
import apros.codeart.rabbitmq.Policy;
import apros.codeart.rabbitmq.RabbitMQConfig;
import apros.codeart.util.LazyIndexer;

public final class RPCConfig {

    private RPCConfig() {
    }

    /**
     * 事件采用的消息的策略
     */
    public static final MQConnConfig ConnConfig;

    /**
     * 持久化队列的server，注意持久化队列，不表示消息也会持续存在，服务端应答完后，消息就被删除了
     */
    public static final Policy ServerPersistent;

    /**
     * 非持久化的
     */
    public static final Policy ServerTransient;

    public static final Policy ClientPolicy;

    static {
        ConnConfig = RabbitMQConfig.find("rpc.@rabbitmq.server");

        // rpc不需要那么高的可靠性，所以不需要发布者确认，也不需要消息持久化
        // 每一个server处理1条消息，处理完后再执行下一条消息
        // 但是可以建立多个server来提高吞吐量，共同承担处理消息的任务
        ServerTransient = new Policy(ConnConfig, 1, false, false, false);

        // 注意，对于rpc服务，就算队列持久化化，消息不需要持久化，因为rpc传递的数据不需要长期保存，请求期间使用就可以了
        ServerPersistent = new Policy(ConnConfig, 1, false, true, false);

        // 客户端是临时队列消费，也一样
        ClientPolicy = new Policy(ConnConfig, 1, false, false, false);
    }

    /**
     * 获得rpc方法对应得配置信息
     *
     * @param method
     * @return
     */
    public static ConsumerConfig getServerConfig(String method) {
        return _getServerConfig.apply(method);
    }

    public static String getServerQueue(String method) {
        return _getServerQueue.apply(method);
    }

    private static final Function<String, String> _getServerQueue = LazyIndexer.init((method) -> {
        return String.format("rpc-%s", method.toLowerCase());
    });

    private static final Function<String, ConsumerConfig> _getServerConfig = LazyIndexer.init((method) -> {
        var maxConcurrency = EchoRPC.section().getInt(String.format("server.maxConcurrency.%s", method), -1);
        if (maxConcurrency < 0)
            maxConcurrency = EchoRPC.section().getInt("server.maxConcurrency", 10);

        return new ConsumerConfig(maxConcurrency);

    });
}
