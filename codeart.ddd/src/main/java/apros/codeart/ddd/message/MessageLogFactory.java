package apros.codeart.ddd.message;

import apros.codeart.ddd.message.internal.FileMessageLogFactory;

public final class MessageLogFactory {
	private MessageLogFactory() {
	}

	private static IMessageLogFactory _factory = FileMessageLogFactory.Instance;

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
