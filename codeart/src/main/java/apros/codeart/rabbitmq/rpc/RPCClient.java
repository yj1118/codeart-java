package apros.codeart.rabbitmq.rpc;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.util.Strings;

import apros.codeart.context.AppSession;
import apros.codeart.dto.DTObject;
import apros.codeart.i18n.Language;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.rpc.client.IClient;
import apros.codeart.pooling.IPoolItem;
import apros.codeart.rabbitmq.IMessageHandler;
import apros.codeart.rabbitmq.Message;
import apros.codeart.rabbitmq.RabbitBus;
import apros.codeart.rabbitmq.RabbitMQException;
import apros.codeart.util.Guid;
import apros.codeart.util.concurrent.LatchSignal;

public class RPCClient implements IClient, AutoCloseable, IMessageHandler {
	private volatile String _correlationId;
	private IPoolItem _busItem;
	private String _tempQueue;
	private LatchSignal<TransferData> _signal;
	private int _secondsTimeout;
	private boolean _success;

	public RPCClient(int secondsTimeout) {
		_busItem = RabbitBus.borrow(RPC.Policy);
		initConsumer();
		_signal = new LatchSignal<TransferData>();
		_secondsTimeout = secondsTimeout;
	}

	private void initConsumer() {
		RabbitBus bus = _busItem.getItem();
		_tempQueue = bus.tempQueueDeclare();
		bus.consume(_tempQueue, this, true);
	}

	public TransferData invoke(String method, DTObject arg) {
		RabbitBus bus = _busItem.getItem();

		_success = false;
		_correlationId = Guid.compact();

		DTObject dto = DTObject.editable();
		dto.setString("method", method);
		dto.combineObject("arg", arg);

		var data = new TransferData(AppSession.language(), dto);
		var routingKey = RPC.getServerQueue(method); // 将服务器端的方法名称作为路由键
		bus.publish(Strings.EMPTY, routingKey, data, (properties) -> {
			properties.replyTo(_tempQueue);
			properties.correlationId(_correlationId);
		});
		var result = _signal.wait(_secondsTimeout, TimeUnit.SECONDS);

		if (!_success) {
			_correlationId = Strings.EMPTY;
			throw new RabbitMQException(Language.strings("codeart", "RequestTimeout", method));
		}

		var info = result.info();

		if (info.getString("status").equals("fail")) {
			var msg = info.getString("message");
			throw new RabbitMQException(msg);
		}

		result.setInfo(info.getObject("returnValue"));
		return result;
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

	public void clear() {
		_correlationId = Strings.EMPTY;
	}

	@Override
	public void close() {
		if (_busItem != null) {
			_busItem.close();
		}
	}

}
