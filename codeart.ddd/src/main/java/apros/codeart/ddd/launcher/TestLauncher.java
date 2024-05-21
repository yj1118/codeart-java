package apros.codeart.ddd.launcher;

import apros.codeart.App;
import apros.codeart.IAppInstaller;

/**
 * 用于测试的启动器
 */
public final class TestLauncher {

	private TestLauncher() {
	}

	public static void start() {
		start(new AppInstaller());
	}

	public static void start(IAppInstaller installer) {

		// 要从框架/子系统/服务宿主 3大块里找定义
		App.init(installer);

		App.initialized();
	}

	public static void stop() {
		App.dispose();
		App.disposed();
	}
}
