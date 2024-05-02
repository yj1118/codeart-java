package apros.codeart.ddd.message.internal;

public final class MessageHost {
	private MessageHost() {
	}

	public static void initialized() {
		MessageProtector.launch();
	}

}
