package apros.codeart.ddd.saga.internal.trigger;

import apros.codeart.ddd.saga.internal.DomainEventHandler;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

/**
 * 收到调用事件的结果的处理器，当调用事件方获取到执行方的结果时会触发该处理器
 */
@SafeAccess
public class ReceiveResultEventHandler extends DomainEventHandler {
	private ReceiveResultEventHandler() {
	}

	@Override
	protected void handle(DTObject event)
	 {
	     try
	     {
	         EventListener.Receive(@event);
	     }
	     catch (EventRestoreException)
	     {
	         //如果抛出的恢复事件发生的异常，那么意味着发生了很严重的错误
	         //此时我们要抛出异常，告诉消息队列不要回复ack,让管理员去处理
	         throw;
	     }
	     catch (Exception ex)
	     {
	         //其他类型的错误写入日志，不抛出异常
	         Logger.Fatal(ex);
	     }
	 }

	public static final ReceiveResultEventHandler instance = new ReceiveResultEventHandler();
}
