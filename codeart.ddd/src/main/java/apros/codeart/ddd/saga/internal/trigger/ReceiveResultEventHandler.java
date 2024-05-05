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
	protected void handle(DTObject e) {
		EventListener.receive(e);
	}

	public static final ReceiveResultEventHandler Instance = new ReceiveResultEventHandler();
}
