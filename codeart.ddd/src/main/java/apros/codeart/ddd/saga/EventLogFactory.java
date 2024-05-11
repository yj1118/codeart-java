package apros.codeart.ddd.saga;

import apros.codeart.ddd.saga.internal.FileEventLogFactory;

public final class EventLogFactory {
	private EventLogFactory() {
	}

	private static IEventLogFactory _factory = FileEventLogFactory.Instance;

	public static IEventLog createLog() {
		return _factory.create();
	}

	public static IEventLogFactory getFactory() {
		return _factory;
	}

	public static void register(IEventLogFactory factory) {
		_factory = factory;
	}

}
