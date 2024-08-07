package apros.codeart.rabbitmq;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

import apros.codeart.dto.DTObject;
import com.rabbitmq.client.AMQP.BasicProperties;

import apros.codeart.echo.TransferData;

public final class Message {

    private final DTObject _content;

    /**
     * 消息的内容
     *
     * @return
     */
    public DTObject content() {
        return _content;
    }
	
    private final BasicProperties _properties;

    /**
     * 消息的属性
     *
     * @return
     */
    public BasicProperties properties() {
        return _properties;
    }

    private final Supplier<Duration> _ack;

    /**
     * 回复消息队列，提示消息已成功处理
     */
    public Duration success() {
        return _ack.get();
    }

    private final Function<Boolean, Duration> _reject;

    /**
     * 回复消息队列，提示消息处理失败
     *
     * @param requeue true:提示RabbitMQ服务器重发消息给下一个订阅者，false:提示RabbitMQ服务器把消息从队列中移除
     */
    public Duration failed(boolean requeue) {
        return _reject.apply(requeue);
    }

    Message(DTObject content, BasicProperties properties, Supplier<Duration> ack,
            Function<Boolean, Duration> reject) {
        _content = content;
        _properties = properties;
        _ack = ack;
        _reject = reject;
    }

}
