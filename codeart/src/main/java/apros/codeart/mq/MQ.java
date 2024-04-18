package apros.codeart.mq;

import apros.codeart.AppConfig;

public final class MQ {
	private MQ() {
	}

	private static class MQHolder {

//		/**
//		 * 消息队列数据传输，用于接收和发送数据的缓冲区大小
//		 */
//		private static final ByteBuffer BUFFER;

		static {

			var mq = AppConfig.section("mq");
			if (mq != null) {
//				var bufferSize = mq.getInt("buffer.size", 10);
//				var bufferCount = mq.getInt("buffer.count", 20);
//
//				BUFFER = ByteBuffer.createMB(bufferSize, bufferCount);

			} else {
//				BUFFER = ByteBuffer.createMB(10, 20);
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

//	public static ByteBuffer getBuffer() {
//		return MQHolder.BUFFER;
//	}

//
//	public static IQueryBuilder getQueryBuilder(Class<? extends IQueryBuilder> qbType) {
//		var agent = DataSource.getAgent();
//		return agent.getQueryBuilder(qbType);
//	}

}
