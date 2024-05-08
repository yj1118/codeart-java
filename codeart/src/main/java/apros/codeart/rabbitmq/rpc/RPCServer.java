package apros.codeart.rabbitmq.rpc;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.util.Strings;

import apros.codeart.context.AppSession;
import apros.codeart.dto.DTObject;
import apros.codeart.log.Logger;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.rpc.server.IRPCHandler;
import apros.codeart.mq.rpc.server.RPCEvents;
import apros.codeart.pooling.IPoolItem;
import apros.codeart.rabbitmq.IMessageHandler;
import apros.codeart.rabbitmq.Message;
import apros.codeart.rabbitmq.RabbitBus;

class RPCServer implements AutoCloseable, IMessageHandler {

	private IRPCHandler _handler;

	private String _queue;

	private String _name;

	public String getName() {
		return _name;
	}

	private IPoolItem _item;

	private RPCServerCluster _cluster;

	private AtomicBoolean _closed;

	public RPCServer(RPCServerCluster cluster, String queue, IRPCHandler handler) {
		_cluster = cluster;
		_queue = queue;
		_handler = handler;
		_closed = new AtomicBoolean(false);
	}

	public void open() {
		if (_item != null)
			return;
		_item = RabbitBus.borrow(RPCConfig.ServerPolicy);
		RabbitBus bus = _item.getItem();
		bus.queueDeclare(_queue);
		bus.consume(_queue, this);
	}

	public void handle(RabbitBus sender, Message msg) {

		AppSession.using(() -> {
			try {
				AppSession.setLanguage(msg.language());

				var content = msg.content();
				var info = content.info();
				var method = info.getString("method");
				var arg = info.getObject("arg");

				var result = process(method, arg);

				var routingKey = msg.properties().getReplyTo(); // 将客户端的临时队列名称作为路由key
				var correlationId = msg.properties().getCorrelationId();
				// 返回结果
				sender.publish(Strings.EMPTY, routingKey, result, (replyProps) -> {
					replyProps.correlationId(correlationId);
				});
			} catch (Exception ex) {
				Logger.fatal(ex);

				var arg = new RPCEvents.ServerErrorArgs(ex);
				RPCEvents.raiseServerError(this, arg);
			} finally {
				// 不论业务上是否报错，始终是对消息处理完毕了，所以success
				var elapsed = msg.success();

				if (_closed.getAcquire()) {
					// 先关闭
					this.dispose();
				}

				_cluster.messagesProcessed(elapsed);
			}
		});
	}

	private TransferData process(String method, DTObject arg) {
		TransferData result;
		DTObject info = DTObject.editable();
		try {
			result = _handler.process(method, arg);

			info.setString("status", "success");
			info.combineObject("returnValue", result.info());

			result.setInfo(info);
		} catch (Exception ex) {
			Logger.fatal(ex);
			info.setString("status", "fail");
			info.setString("message", ex.getMessage());
			result = new TransferData(AppSession.language(), info);
		}
		return result;
	}

	public boolean disposed() {
		return _item == null;
	}

	public void dispose() {
		if (_item != null) {
			_item.close();
			_item = null;
		}
	}

	/**
	 * 直到下次处理完一个请求后才会真正关闭
	 */
	@Override
	public void close() {
		_closed.setRelease(true);
	}
}
