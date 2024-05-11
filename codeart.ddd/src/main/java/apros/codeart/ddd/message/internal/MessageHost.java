package apros.codeart.ddd.message.internal;

import apros.codeart.ModuleInstaller;
import apros.codeart.ddd.message.MessageConfig;
import apros.codeart.ddd.message.MessageLogFactory;

public final class MessageHost {
	private MessageHost() {
	}

	public static void initialize() {
		setupMessageLog();
		AtomicOperation.init();
		MessageLogFactory.getFactory().init();
	}

	private static void setupMessageLog() {
		var provider = MessageConfig.section().getString("log.provider", null);
		ModuleInstaller.setup(provider, FileMessageLogProvider.class);
	}

	public static void initialized() {
		MessageProtector.launch();
	}

}
