package apros.codeart.ddd;

import apros.codeart.TestSupport;

public final class DDDConfig {

	private DDDConfig() {

	}

//	private static final DTObject _objectMeta;
//
//	/**
//	 * objectMeta.json 里的配置
//	 * 
//	 * @return
//	 */
//	public static final DTObject objectMeta() {
//		return _objectMeta;
//	}

//	var db = AppConfig.section("repository.db");
//	if (db != null) {
//		TYPE = getType(db.getString("type"));
//
//		AGENT = getAgent(TYPE);
//
//		// 创建HikariConfig配置对象
//		HikariConfig config = new HikariConfig();
//		config.setJdbcUrl(db.getString("url"));
//		// 可以添加更多配置
//
//		// 使用配置创建HikariDataSource实例
//		INSTANCE = new HikariDataSource(config);
//	} else {
//		INSTANCE = null;
//		TYPE = null;
//		AGENT = null;
//	}

	// 以后可以追加设置成默认从配置文件里加载，同时程序里可以改值
	private static boolean _enableReset;

	/**
	 * 开启重置功能，每次启动领域模块则清空所有资源
	 */
	@TestSupport
	public static void enableReset(boolean value) {
		_enableReset = value;
	}

	@TestSupport
	public static boolean enableReset() {
		return _enableReset;
	}

	static {
//		_objectMeta = ResourceUtil.loadJSON("codeart/objectMeta.json");

	}
}
