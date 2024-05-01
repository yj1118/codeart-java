package apros.codeart.ddd.saga;

import apros.codeart.AppConfig;
import apros.codeart.InterfaceImplementer;
import apros.codeart.dto.DTObject;

final class SAGAConfig {

	private SAGAConfig() {

	}

	private static InterfaceImplementer _logFactoryImplementer;

	/**
	 * 
	 * 领域事件日志工厂的实现
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

		_retainDays = root.getInt("etain", 0); // 默认永久保留
	}

	private static int _retainDays;

	public static int retainDays() {
		return _retainDays;
	}

	static {

		var saga = AppConfig.section("saga");
		if (saga != null) {
			loadEventLog(saga);
		} else {
			_retainDays = 0;
		}

	}
}
