package apros.codeart.ddd.cqrs.internal;

import apros.codeart.AppConfig;
import apros.codeart.dto.DTObject;

public final class CQRSConfig {

	private CQRSConfig() {

	}

	private static boolean _fock;

	public static boolean fock() {
		return _fock;
	}

	private static void init(DTObject config) {
		_fock = config.getBoolean("fock", false);
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
			_fock = false;
		}

	}
}
