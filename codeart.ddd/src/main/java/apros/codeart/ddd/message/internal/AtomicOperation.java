package apros.codeart.ddd.message.internal;

import java.util.List;

import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.DataAccess;
import apros.codeart.ddd.repository.access.DataSource;
import apros.codeart.ddd.repository.access.DatabaseType;
import apros.codeart.util.ListUtil;

public final class AtomicOperation {
	private AtomicOperation() {
	}

	public static void init() {
		// 此处要初始化领域消息表
		var sql = getInitSql();
		DataContext.newScope((conn) -> {
			conn.access().execute(sql);
		});
	}

	private static String getInitSql() {
		switch (DataSource.getDatabaseType()) {
		case DatabaseType.PostgreSql: {
			// todo
			return null;
		}
		case DatabaseType.MySql: {
			// todo
			return null;
		}
		case DatabaseType.Oracle: {
			// todo
			return null;
		}
		case DatabaseType.SqlServer: {

			return """
					if ISNULL(object_id(N'[dbo].[CA_DomainMessage]'),'') = 0
					begin
						CREATE TABLE [dbo].[CA_DomainMessage](
						[id] [uniqueidentifier] NOT NULL,
					 CONSTRAINT [PK_CA_DomainMessage] PRIMARY KEY CLUSTERED
					(
						[id] ASC
					)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
					) ON [PRIMARY]
					end
					""";
		}
		}
		return null;
	}

	public static void insert(String msgId) {
		// 注意，写入文件前，先要写入数据库数据
		DataContext.using((conn) -> {
			// 跟主程序同一个事务，确保两者都被同时提交
			insert(conn.access(), msgId);
		});
	}

	private static void insert(DataAccess access, String msgId) {
		switch (DataSource.getDatabaseType()) {
		case DatabaseType.PostgreSql: {
			// todo
			break;
		}
		case DatabaseType.MySql: {
			// todo
			break;
		}
		case DatabaseType.Oracle: {
			// todo
			break;
		}
		case DatabaseType.SqlServer: {

			access.execute("""
					INSERT INTO [dbo].[CA_DomainMessage]
					          ([id])
					    VALUES
					          '%s');
					""".formatted(msgId));

			break;
		}
		}
	}

	public static void delete(String msgId) {
		// 删除文件后，删除数据库数据
		DataContext.newScope((conn) -> {
			delete(conn.access(), msgId);
		});
	}

	private static void delete(DataAccess access, String msgId) {
		switch (DataSource.getDatabaseType()) {
		case DatabaseType.PostgreSql: {
			// todo
			break;
		}
		case DatabaseType.MySql: {
			// todo
			break;
		}
		case DatabaseType.Oracle: {
			// todo
			break;
		}
		case DatabaseType.SqlServer: {

			access.execute(String.format("DELETE FROM [dbo].[CA_DomainMessage] WHERE id='%s';", msgId));

			break;
		}
		}
	}

	public static List<String> findInterrupteds() {
		// 注意，要从数据库里读取，只有数据库里的是必须要发送的，而不是文件里存储的
		var ids = DataContext.using((conn) -> {
			return findInterrupteds(conn.access());
		});
		return ListUtil.asList(ids);
	}

	private static Iterable<String> findInterrupteds(DataAccess access) {
		switch (DataSource.getDatabaseType()) {
		case DatabaseType.PostgreSql: {
			// todo
			return null;
		}
		case DatabaseType.MySql: {
			// todo
			return null;
		}
		case DatabaseType.Oracle: {
			// todo
			return null;
		}
		case DatabaseType.SqlServer: {

			return access.queryScalars(String.class, "SELECT id FROM [dbo].[CA_DomainMessage];", null);

		}
		}
		return null;
	}

}
