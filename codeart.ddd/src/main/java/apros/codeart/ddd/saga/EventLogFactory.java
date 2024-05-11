package apros.codeart.ddd.saga;

public final class EventLogFactory {
	private EventLogFactory() {
	}

	private static IEventLogFactory _factory;

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
