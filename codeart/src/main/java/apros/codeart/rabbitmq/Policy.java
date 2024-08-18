package apros.codeart.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.StringUtil;

public final class Policy {

    private final MQConnConfig _connConfig;

    /**
     * 该策略用到的RabbitMQ服务相关的信息
     *
     * @return
     */
    public MQConnConfig connConfig() {
        return _connConfig;
    }

    private final int _prefetchCount;

    /**
     * 如果PrefetchCount设置为5，表示最多同时处理5个消息，多余的消息RabbitMQ会堆积在服务器或者给其他的消费者处理
     * <p>
     * 根据消费者的处理速度和网络条件，适当增加 prefetchCount 可以减少网络往返次数，从而提高整体的消息处理吞吐量。
     *
     * @return
     */
    public int prefetchCount() {
        return _prefetchCount;
    }

    private final boolean _publisherConfirms;

    /**
     * 发送方确认模式，一旦信道进入该模式，所有在信道上发布的消息都会被指派以个唯一的ID号（从1开始）。
     * 一旦消息被成功投递给所匹配的队列后，信道会发送一个确认给生产者（包含消息的唯一ID）。这使得生产者
     * 知晓消息已经安全达到目的队列了。如果消息和队列是可持久化的，那么确认消息只会在队列将消息写入磁盘后才会发出。
     * 如果RabbitMQ服务器发生了宕机或者内部错误导致了消息的丢失，Rabbit会发送一条nack消息给生产者，表明消息已经丢失。
     * 该模式性能优秀可以取代性能低下的发送消息事务机制。 在需要严谨的、消息必须送达的情况下需要开启该模式。
     * <p>
     * <p>
     * 注意，发布者确认模式，何时确认是rabbitMQ服务器的处理，跟程序员无关，程序员不需要手工去确认。这跟消息确认模式不同。
     *
     * @return
     */
    public boolean publisherConfirms() {
        return _publisherConfirms;
    }

    private final boolean _persistentQueue;

    /**
     * 队列是否持久化：
     * 持久化队列在 RabbitMQ 服务器重启后会继续存在，即使服务器重启，队列本身不会丢失。
     * <p>
     * 但是如果消息非持久化：这些消息只会保存在内存中，而不会写入磁盘。
     *
     * @return
     */
    public boolean persistentQueue() {
        return _persistentQueue;
    }

    private final boolean _persistentMessage;

    /**
     * 是否持久化消息：
     * 持久化消息会被写入磁盘，以确保在 RabbitMQ 服务重启后，消息不会丢失。
     * <p>
     * 但是注意：队列持久化与消息持久化是独立的：即使队列是持久化的，如果消息没有被标记为持久化，
     * 该消息在 RabbitMQ 重启后依然会丢失。同样地，非持久化队列即使消息标记为持久化，消息也不会被保存。
     *
     * @return
     */
    public boolean persistentMessage() {
        return _persistentMessage;
    }

    private final String _connectionString;

    public String connectionString() {
        return _connectionString;
    }

    public Policy(MQConnConfig connConfig, int prefetchCount, boolean publisherConfirms, Boolean persistentQueue, boolean persistentMessage) {
        _connConfig = connConfig;
        _prefetchCount = prefetchCount;
        _publisherConfirms = publisherConfirms;
        _persistentQueue = persistentQueue;
        _persistentMessage = persistentMessage;
        _connectionString = getConnectionString();
    }

    private String getConnectionString() {
        StringBuilder code = new StringBuilder();
        StringUtil.appendFormat(code, "host=%s;", this.connConfig().host());
        StringUtil.appendFormat(code, "virtualHost=%s;", this.connConfig().virtualHost());
        StringUtil.appendFormat(code, "username=%s;", this.connConfig().username());
        StringUtil.appendFormat(code, "password=%s;", this.connConfig().password());
        StringUtil.appendFormat(code, "prefetchcount=%s;", this.prefetchCount());

        if (this.publisherConfirms()) {
            code.append("publisherConfirms=true;");
        } else
            code.append("publisherConfirms=false;");

        if (this.persistentQueue()) {
            code.append("persistentQueue=true;");
        } else
            code.append("persistentQueue=false;");

        if (this.persistentMessage()) {
            code.append("persistentMessage=true;");
        } else
            code.append("persistentMessage=false;");

        return code.toString();
    }

    @Override
    public int hashCode() {
        return _connectionString.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        var target = TypeUtil.as(obj, Policy.class);
        if (target == null)
            return false;
        return target.connectionString().equals(this.connectionString());
    }

    void init(ConnectionFactory factory) {
        factory.setHost(this.connConfig().host());
        factory.setVirtualHost(this.connConfig().virtualHost());
        factory.setUsername(this.connConfig().username());
        factory.setPassword(this.connConfig().password());
    }
}
