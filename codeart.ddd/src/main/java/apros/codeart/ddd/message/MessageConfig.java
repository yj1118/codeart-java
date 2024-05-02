package apros.codeart.ddd.message;

import apros.codeart.AppConfig;
import apros.codeart.InterfaceImplementer;
import apros.codeart.dto.DTObject;

class MessageConfig {
	private MessageConfig() {

	}

	private static InterfaceImplementer _logFactoryImplementer;

	/**
	 * 
	 * 领域消息日志工厂的实现
	 * 
	 * @return
	 */
	public static InterfaceImplementer logFactoryImplementer() {
		return _logFactoryImplementer;
	}

	private static void loadEventLog(DTObject root) {
		var factory = root.getObject("log.factory", null);

		if (factory != null)
			_logFactoryImplementer = InterfaceImplementer.create(factory);

	}

	private static DTObject _section;

	public static DTObject section() {
		return _section;
	}

	static {

		var _section = AppConfig.section("message");
		if (_section != null) {
			loadEventLog(_section);
		} else {
			_section = DTObject.Empty;
		}

	}
}
