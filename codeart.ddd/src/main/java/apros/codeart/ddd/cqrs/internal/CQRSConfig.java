package apros.codeart.ddd.cqrs.internal;

import apros.codeart.AppConfig;
import apros.codeart.dto.DTObject;

public final class CQRSConfig {

	private CQRSConfig() {

	}

	private static boolean _master;

	public static boolean master() {
		return _master;
	}

	private static void init(DTObject config) {
		_master = config.getBoolean("master", false);
	}

	private static DTObject _section;

	public static DTObject section() {
		return _section;
	}

	static {

		_section = AppConfig.section("cqrs");
		if (_section != null) {
			init(_section);
		} else {
			_section = DTObject.Empty;
			_master = false;
		}

	}
}
