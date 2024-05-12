package apros.codeart.ddd.message.internal;

import apros.codeart.App;
import apros.codeart.ddd.message.MessageLogFactory;

public final class MessageHost {
	private MessageHost() {
	}

	public static void initialize() {
		setupMessageModule();
		AtomicOperation.init();
		MessageLogFactory.getFactory().init();
	}

	public static void initialized() {
		MessageProtector.launch();
	}

	/**
	 * 安装领域消息的模块配置
	 */
	private static void setupMessageModule() {
		App.setup("message");
	}

}
