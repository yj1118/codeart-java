package apros.codeart.ddd.saga;

import apros.codeart.AppConfig;
import apros.codeart.dto.DTObject;

public final class SAGAConfig {

	private SAGAConfig() {

	}

	private static void loadEventLog(DTObject root) {
		_retainDays = root.getInt("@log.retain", 0); // 默认永久保留
	}

	private static int _retainDays;

	public static int retainDays() {
		return _retainDays;
	}

	private static DTObject _section;

	public static DTObject section() {
		return _section;
	}

	static {

		_section = AppConfig.section("saga");
		if (_section != null) {
			loadEventLog(_section);
		} else {
			_section = DTObject.Empty;
			_retainDays = 0;
		}

	}
}
