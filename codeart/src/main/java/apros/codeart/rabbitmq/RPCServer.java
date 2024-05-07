package apros.codeart.rabbitmq;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.util.Strings;

import apros.codeart.context.AppSession;
import apros.codeart.dto.DTObject;
import apros.codeart.log.Logger;
import apros.codeart.mq.TransferData;
import apros.codeart.mq.rpc.server.IRPCHandler;
import apros.codeart.mq.rpc.server.IServer;
import apros.codeart.mq.rpc.server.RPCEvents;
import apros.codeart.pooling.IPoolItem;

public class RPCServer implements IServer, AutoCloseable, IMessageHandler {

	private IRPCHandler _handler;

	private String _queue;

	private String _name;

	public String getName() {
		return _name;
	}

	private ConcurrentLinkedQueue<IPoolItem> _items;

	public RPCServer(String method) {
		_name = method;
		_items = new ConcurrentLinkedQueue<IPoolItem>();
		_queue = RPC.getServerQueue(method);
		_closed = new AtomicBoolean(false);
	}

	public void initialize(IRPCHandler handler) {
		_handler = handler;
	}

	public void open() {
		open(1);
	}

	private void open(int count) {
		synchronized (_items) {
			if (_closed.getAcquire())
				return;

			for (var i = 0; i < count; i++) {
				var item = RabbitBus.borrow(RPC.Policy);
				RabbitBus host = item.getItem();
				host.queueDeclare(_queue);
				host.consume(_queue, this);

				_items.offer(item);
			}
		}
	}

	// todo 稍后作成配置
	private final int MAX = 100;

	private void tryScale() {

		var messageCount = RabbitBus.getMessageCount(RPC.Policy, _queue);
		var size = _items.size();
		if (messageCount > size && size < MAX) {
			// 等待处理的消息数量大于订阅者，那么扩容1.5倍
			int half = Math.max(size >> 1, 1);
			int count = Math.min(size + half, MAX) - size;
			this.open(count);
		} else {
			int half = Math.max(size >> 1, 1);
			// 当消息数少于0.5倍时，减少
			if (messageCount < (size - half) && size > 1) {
				// 减少0.5倍
				this.close(half);
			}
		}
	}

	private void close(int count) {
		synchronized (_items) {
			if (_closed.getAcquire())
				return;

			for (var i = 0; i < count; i++) {
				// 至少保留一个
				if (_items.size() == 1)
					return;

				var item = _items.poll();
				if (item != null)
					item.close();
			}
		}
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
				sender.publish(Strings.EMPTY, routingKey, result, (replyProps) -> {
					replyProps.correlationId(correlationId);
				});
			} catch (Exception ex) {
				Logger.fatal(ex);

				var arg = new RPCEvents.ServerErrorArgs(ex);
				RPCEvents.RaiseServerError(this, arg);
			} finally {
				msg.success();
			}
		});
	}

	private TransferData process(String method, DTObject arg) {
		TransferData result;
		DTObject info = DTObject.Create();
		try {
			result = _handler.Process(method, arg);

			info["status"] = "success";
			info["returnValue"] = result.Info;

			result.Info = info;
		} catch (Exception ex) {
			Logger.Fatal(ex);
			info["status"] = "fail";
			info["message"] = ex.Message;
			result = new TransferData(AppSession.Language, info);
		}
		return result;
	}

	private AtomicBoolean _closed;

	@Override
	public void close() {
		// 虽然close做了一定的并发处理，但是不够完美，不过也无所谓，因为这是在关闭服务，相当于关闭整个程序
		if (_closed.compareAndExchange(false, true)) {
			synchronized (_items) {
				for (var item : _items) {
					item.close();
				}
				_items.clear();
			}
		}
	}
}
