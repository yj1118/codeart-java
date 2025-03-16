package apros.codeart.rabbitmq.rpc;

import java.time.Duration;

import apros.codeart.rabbitmq.Policy;
import apros.codeart.util.SafeAccess;
import org.apache.logging.log4j.util.Strings;

import apros.codeart.context.AppSession;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.rpc.IRPCHandler;
import apros.codeart.echo.rpc.RPCEvents;
import apros.codeart.log.Logger;
import apros.codeart.pooling.IPoolItem;
import apros.codeart.rabbitmq.Consumer;
import apros.codeart.rabbitmq.Message;
import apros.codeart.rabbitmq.RabbitBus;

/**
 * 用基于消息队列的rpc服务端的好处是稳定，服务自动发现
 */
@SafeAccess
class RPCServer extends Consumer implements AutoCloseable {

    private final IRPCHandler _handler;

    private final String _queue;

    private IPoolItem _item;

    private final Policy _policy;

    public RPCServer(RPCServerCluster cluster, String queue, IRPCHandler handler, Policy policy) {
        super(cluster);
        _queue = queue;
        _handler = handler;
        _policy = policy;
    }

    @Override
    public void open() {
        if (_item != null)
            return;
        // 注意，原本policy是RPCConfig.ServerPolicy，这是一个不持久化消息的服务端策略
        // 但是，我们在一些情况下有可能rpc客户端先发送请求，服务端后启动后响应（比如：各个服务启动时，需要向依赖服务请求领域对象元数据，由于不能确保服务按顺序启动，所以需要client请求后持续等待，服务端启动后再发送数据）
        // 这时候就需要rpc的队列持久化了，所以这里接收的是外部设置的策略
        _item = RabbitBus.borrow(_policy);
        RabbitBus bus = _item.getItem();
        bus.queueDeclare(_queue);
        bus.consume(_queue, this);
    }

    @Override
    protected Duration processMessage(RabbitBus sender, Message msg) {

        return AppSession.using(() -> {
            Duration elapsed = null;
            var routingKey = msg.properties().getReplyTo(); // 将客户端的临时队列名称作为路由key
            var correlationId = msg.properties().getCorrelationId();

            try {
                var content = msg.content();
                var method = content.getString("method");
                var arg = content.getObject("arg");
                var language = content.getString("language");

                AppSession.setLanguage(language);

                var result = process(method, arg);

                // 返回结果
                sender.publish(Strings.EMPTY, routingKey, result, (replyProps) -> {
                    replyProps.correlationId(correlationId);
                });
            } catch (Throwable ex) {
                Logger.error(ex);

                var arg = new RPCEvents.ServerErrorArgs(ex);
                RPCEvents.raiseServerError(this, arg);

                var error = processError(ex);
                // 向客户端提示错误
                sender.publish(Strings.EMPTY, routingKey, error, (replyProps) -> {
                    replyProps.correlationId(correlationId);
                });
            } finally {
                // 不论业务上是否报错，始终是对消息处理完毕了，所以success
                elapsed = msg.success();
            }
            return elapsed;
        });
    }
    
    private DTObject process(String method, DTObject arg) {
        DTObject body = DTObject.editable();
        try {
            var result = _handler.process(method, arg);
            body.combineObject("data", result);
        } catch (Throwable ex) {
            Logger.error(ex);
            body.setString("error", ex.getMessage());
        }
        return body;
    }

    private DTObject processError(Throwable ex) {
        DTObject body = DTObject.editable();
        body.setString("error", ex.getMessage());
        return body;
    }

    @Override
    public boolean disposed() {
        return _item == null;
    }

    public void dispose() {
        if (_item != null) {
            _item.close();
            _item = null;
        }
    }
}
