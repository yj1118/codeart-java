package apros.codeart.ddd.message.internal;

import apros.codeart.ddd.message.MessageLogFactory;

public final class MessageHost {
	private MessageHost() {
	}

	public static void initialize() {
		AtomicOperation.init();
		MessageLogFactory.getFactory().init();
	}

	public static void initialized() {
		MessageProtector.launch();
	}

}
