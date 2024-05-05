package apros.codeart.ddd.saga.internal.protector;

import apros.codeart.ddd.saga.internal.DomainEventHandler;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

/**
 * 收到回溯的请求
 */
@SafeAccess
public final class ReverseEventHandler extends DomainEventHandler {
	private ReverseEventHandler() {
	}

	@Override
	protected void handle(DTObject e) {
		EventListener.reverse(e);
	}

	public static final ReverseEventHandler Instance = new ReverseEventHandler();
}
