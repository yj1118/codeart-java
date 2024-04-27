package apros.codeart.ddd.saga;

import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

/**
 * 触发事件的处理器，当服务端收到了触发事件的通知时会用此对象处理请求
 */
@SafeAccess
class RaiseEventHandler extends DomainEventHandler {
	private RaiseEventHandler() {
	}

	@Override
	protected void handle(DTObject arg)
	  {
	      try
	      {
	          EventListener.Accept(arg);
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

	public static readonly RaiseEventHandler Instance=new RaiseEventHandler();
}
