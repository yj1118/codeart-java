package apros.codeart.rabbitmq;

import apros.codeart.mq.rpc.server.IRPCHandler;
import apros.codeart.mq.rpc.server.IServer;

public class RPCServer implements IServer, AutoCloseable, IMessageHandler {
	private IPoolItem _hostItem;
	private IRPCHandler _handler;
	private String _queue;

	private String _name;

	public String name() {
		return _name;
	}

	public RPCServer(String method) {
		_name = method;
//		_hostItem = RabbitBus.Borrow(RPC.Policy);
//		_queue = RPC.GetServerQueue(method);
	}

	public void Initialize(IRPCHandler handler) {
		_handler = handler;
	}

	public void Open() {
		var host = _hostItem.Item;
		host.QueueDeclare(_queue);
		host.Consume(_queue, this);
	}

	void IMessageHandler.Handle(Message message)
	{
	    var bus = _hostItem.Item;

	    AppSession.Using(() =>
	    {
	        try
	        {
	            AppSession.Language = message.Language;

	            var content = message.Content;
	            var info = content.Info;
	            var method = info.GetValue<string>("method");
	            var arg = info.GetObject("arg");

	            var result = Process(method, arg);

	            var routingKey = message.Properties.ReplyTo; //将客户端的临时队列名称作为路由key
	            bus.Publish(string.Empty, routingKey, result, (replyProps) =>
	            {
	                replyProps.CorrelationId = message.Properties.CorrelationId;
	            });
	        }
	        catch (Exception ex)
	        {
	            Logger.Fatal(ex);

	            var arg = new RPCEvents.ServerErrorArgs(ex);
	            RPCEvents.RaiseServerError(this, arg);
	        }
	        finally
	        {
	            message.Success();
	        }
	    }, true);
	}

	private TransferData Process(string method, DTObject arg) {
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

	public void Close() {
		this.Dispose();
	}

	public void Dispose() {
		_hostItem.Dispose();
	}
}
