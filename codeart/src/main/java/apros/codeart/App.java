package apros.codeart;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

import apros.codeart.runtime.Activator;
import apros.codeart.util.ArgumentAssert;
import apros.codeart.util.ListUtil;

public final class App {
	private App() {
	}

	private static String[] _archives;

	public static String[] archives() {
		return _archives;
	}

	private static IAppInstaller _installer;

	public static void setup(String moduleName) {
		_installer.setup(moduleName, null);
	}

	public static void setup(String moduleName, Object[] args) {
		_installer.setup(moduleName, args);
	}

	public static void init() {
		init(DefautlAppInstaller.Instance);
	}

	/**
	 * 应用程序初始化，请根据不同的上下文环境，在程序入口处调用此方法
	 * 
	 * @param archives 需要参与初始化的档案名称，档案是包的顶级名称，比如
	 *                 subsystem.account和subsystem.user的档案名为subsystem
	 */
	public static void init(IAppInstaller installer) {

		_archives = installer.getArchives();

		ArgumentAssert.isNotNullOrEmpty(_archives, "archives");

		installer.init();

		_installer = installer;

		process_pre_start();
	}

	private static void process_pre_start() {
		runActions(PreApplicationStart.class);
	}

	private static AtomicBoolean _post_start_completed = new AtomicBoolean(false);

	public static boolean started() {
		return _post_start_completed.getAcquire();
	}

	/// <summary>
	/// 应用程序初始化完后，请根据不同的上下文环境，在程序入口处调用此方法
	/// </summary>
	public static void initialized() {

		process_post_start();
		// 清理安装器的资源
		_installer.dispose();

		_post_start_completed.setRelease(true);
	}

	private static void process_post_start() {
		runActions(PostApplicationStart.class);
	}

	public static void dispose() {
		process_pre_end();
	}

	private static void process_pre_end() {
		runActions(PreApplicationEnd.class);
	}

	public static void disposed() {
		process_post_end();
	}

	private static void process_post_end() {
		runActions(PostApplicationEnd.class);
	}

	private static void runActions(Class<? extends Annotation> annType) {
		var items = ListUtil.map(Activator.getAnnotatedTypesOf(annType, _archives), (type) -> {
			var ann = type.getAnnotation(PreApplicationStart.class);
			return new ActionItem(type, ann.method());
		});

		for (var item : items) {
			item.run();
		}
	}

	private static class DefautlAppInstaller extends AppInstallerBase {

		private DefautlAppInstaller() {
		}

		@Override
		public void init() {
			// TODO Auto-generated method stub

		}

		@Override
		public String[] getArchives() {
			return AppConfig.mergeArchives("codeart");
		}

		@Override
		public void setup(String moduleName, Object[] args) {

		}

		@Override
		public void dispose() {

		}

		public static final DefautlAppInstaller Instance = new DefautlAppInstaller();

	}

}
