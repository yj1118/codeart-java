package apros.codeart.ddd.saga.internal.trigger;

import apros.codeart.ddd.saga.internal.DomainEventHandler;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

/**
 * 触发事件的处理器，当服务端收到了触发事件的通知时会用此对象处理请求
 */
@SafeAccess
public class RaiseEventHandler extends DomainEventHandler {
	private RaiseEventHandler() {
	}

	@Override
	protected void handle(DTObject e) {
		EventListener.accept(e);
	}

	public static final RaiseEventHandler instance = new RaiseEventHandler();
}
