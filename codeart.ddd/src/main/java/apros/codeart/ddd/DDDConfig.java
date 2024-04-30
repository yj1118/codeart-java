package apros.codeart.ddd;

import apros.codeart.AppConfig;
import apros.codeart.InterfaceImplementer;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ResourceUtil;

public final class DDDConfig {

	private DDDConfig() {

	}

	private static final DTObject _objectMeta;

	/**
	 * objectMeta.json 里的配置
	 * 
	 * @return
	 */
	public static final DTObject objectMeta() {
		return _objectMeta;
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
		_objectMeta = ResourceUtil.loadJSON("codeart/objectMeta.json");

		var saga = AppConfig.section("saga");
		if (saga != null) {
			loadEventLog(saga);
		}

	}
}
