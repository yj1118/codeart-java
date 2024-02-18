package apros.codeart;

public final class App {
	private App() {
	}

	private static boolean _initialized;

	/// <summary>
	/// 应用程序初始化，请根据不同的上下文环境，在程序入口处调用此方法
	/// </summary>
	public static void initialize() {
		if (_initialized)
			return;
		process_start();
	}

	private static void process_start() {

	}

	private static boolean _cleanup = false;

	public static void cleanup() {
		if (_cleanup)
			return;
		process_end();
	}

	private static void process_end() {

	}

}
