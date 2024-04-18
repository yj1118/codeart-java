package apros.codeart.mq;

import apros.codeart.AppConfig;

public final class MQConfig {
	private MQConfig() {
	}

	private static class MQConfigHolder {

		/**
		 * 消息队列数据传输，用于接收和发送数据的缓冲区大小
		 */
		private static final int BUFFER_SIZE;

		static {

			var mq = AppConfig.section("mq");
			if (mq != null) {
				BUFFER_SIZE = mq.getInt("buffer");

			} else {
				BUFFER_SIZE = 200; // 默认200M
			}
		}
	}

//	public static Connection getConnection() {
//		try {
//			return DataSourceHolder.INSTANCE.getConnection();
//		} catch (SQLException e) {
//			throw propagate(e);
//		}
//	}
//
//	public static DatabaseType getDatabaseType() {
//		return DataSourceHolder.TYPE;
//	}
//
	/**
	 * 
	 * 字节缓冲区的大小
	 * 
	 * @return
	 */
	public static int bufferSize() {
		return MQConfigHolder.BUFFER_SIZE;
	}
//
//	public static IQueryBuilder getQueryBuilder(Class<? extends IQueryBuilder> qbType) {
//		var agent = DataSource.getAgent();
//		return agent.getQueryBuilder(qbType);
//	}

}
