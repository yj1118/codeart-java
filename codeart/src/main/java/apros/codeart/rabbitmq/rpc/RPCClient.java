package apros.codeart.rabbitmq.rpc;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.util.Strings;

import apros.codeart.context.AppSession;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.rpc.IClient;
import apros.codeart.i18n.Language;
import apros.codeart.pooling.IPoolItem;
import apros.codeart.rabbitmq.IMessageHandler;
import apros.codeart.rabbitmq.Message;
import apros.codeart.rabbitmq.RabbitBus;
import apros.codeart.rabbitmq.RabbitMQException;
import apros.codeart.util.GUID;
import apros.codeart.util.StringUtil;
import apros.codeart.util.concurrent.LatchSignal;

public class RPCClient implements IClient, AutoCloseable, IMessageHandler {
    private volatile String _correlationId;
    private final IPoolItem _busItem;
    private String _tempQueue;
    private final LatchSignal<DTObject> _signal;
    private final int _secondsTimeout;
    private boolean _success;

    public RPCClient(int secondsTimeout) {
        _busItem = RabbitBus.borrow(RPCConfig.ClientPolicy);
        initConsumer();
        _signal = new LatchSignal<>();
        _secondsTimeout = secondsTimeout;
    }

    private void initConsumer() {
        RabbitBus bus = _busItem.getItem();
        _tempQueue = bus.tempQueueDeclare();
        bus.consume(_tempQueue, this);
    }

    public DTObject invoke(String method, DTObject arg) {
        RabbitBus bus = _busItem.getItem();

        _success = false;
        _correlationId = GUID.compact();

        DTObject data = DTObject.editable();
        data.setString("method", method);
        data.combineObject("arg", arg);
        data.setString("language", AppSession.language());

//        var data = new TransferData(AppSession.language(), dto);
        var routingKey = RPCConfig.getServerQueue(method); // 将服务器端的方法名称作为路由键
        bus.publish(Strings.EMPTY, routingKey, data, (properties) -> {
            properties.replyTo(_tempQueue);
            properties.correlationId(_correlationId);
        });
        var result = _signal.wait(_secondsTimeout, TimeUnit.SECONDS);

        if (!_success) {
            _correlationId = Strings.EMPTY;
            throw new RabbitMQException(Language.strings("apros.codeart", "RequestTimeout", method));
        }

        var error = result.getString("error", null);

        if (!StringUtil.isNullOrEmpty(error)) {
            throw new RabbitMQException(error);
        }

        return result.getObject("data");
    }

    @Override
    public void handle(RabbitBus sender, Message message) {
        if (_correlationId.equalsIgnoreCase(message.properties().getCorrelationId())) {
            message.success();
            var result = message.content();
            _success = true;
            _signal.set(result);
        }
    }

    /**
     * 清理客户端资源，以便可以重用
     */
    @Override
    public void clear() {
        _correlationId = Strings.EMPTY;
        // _busItem 不用释放，可以继续使用
    }

    @Override
    public void close() {
        if (_busItem != null) {
            _busItem.close();
        }
    }

}
