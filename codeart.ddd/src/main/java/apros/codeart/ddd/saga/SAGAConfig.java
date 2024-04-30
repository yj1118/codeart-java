package apros.codeart.ddd.saga;

import apros.codeart.AppConfig;
import apros.codeart.InterfaceImplementer;
import apros.codeart.dto.DTObject;

public final class SAGAConfig {

	private SAGAConfig() {

	}

	private static InterfaceImplementer _eventLogFactoryImplementer;

	/**
	 * 
	 * 领域事件日志工厂的实现
	 * 
	 * @return
	 */
	public static InterfaceImplementer eventLogFactoryImplementer() {
		return _eventLogFactoryImplementer;
	}

	private static void loadEventLog(DTObject root) {
		var factory = root.getObject("log.factory", null);

		if (factory != null)
			_eventLogFactoryImplementer = InterfaceImplementer.create(factory);
	}

	static {

		var saga = AppConfig.section("saga");
		if (saga != null) {
			loadEventLog(saga);
		}

	}
}
