package apros.codeart.ddd.message;

public final class MessageLogFactory {
	private MessageLogFactory() {
	}

	private static IMessageLogFactory _factory;

	public static IMessageLog createLog() {
		return _factory.create();
	}

	public static IMessageLogFactory getFactory() {
		return _factory;
	}

	public static void register(IMessageLogFactory factory) {
		_factory = factory;
	}

}
