package apros.codeart.ddd.message.internal;

import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.DataAccess;
import apros.codeart.ddd.repository.access.DataSource;
import apros.codeart.ddd.repository.access.DatabaseType;
import apros.codeart.ddd.saga.internal.EventLog;
import apros.codeart.util.ListUtil;
import apros.codeart.util.thread.Timer;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MessageFilter {
    private MessageFilter() {
    }

    public static void init() {
        // 此处要初始化消息过滤表
        var sql = getInitSql();
        DataContext.newScope((conn) -> {
            conn.access().nativeExecute(sql);
        });
    }

    private static String getInitSql() {
        switch (DataSource.getDatabaseType()) {
            case DatabaseType.PostgreSql: {
                return """
                        CREATE TABLE IF NOT EXISTS "CA_DomainMessageFilter" (
                               "Id" CHAR(32) PRIMARY KEY,
                               "CreateTime" timestamp(0) DEFAULT CURRENT_TIMESTAMP NOT NULL
                           );
                        """;
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
                        if ISNULL(object_id(N'[dbo].[CA_DomainMessageFilter]'),'') = 0
                        begin
                        	CREATE TABLE [dbo].[CA_DomainMessageFilter](
                        	[Id] [char(32)] NOT NULL,
                        	[CreateTime] DATETIME DEFAULT GETDATE() NOT NULL,
                         CONSTRAINT [PK_CA_DomainMessageFilter] PRIMARY KEY CLUSTERED
                        (
                        	[Id] ASC
                        )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
                        ) ON [PRIMARY]
                        end
                        """;
            }
        }
        return null;
    }

    public static boolean exist(String msgId) {
        // 注意，写入文件前，先要写入数据库数据
        return DataContext.using((conn) -> {
            // 跟主程序同一个事务，确保两者都被同时提交
            return exist(conn.access(), msgId);
        }, true); //注意，要进入事务保护，所以打开立即模式
    }

    private static boolean exist(DataAccess access, String msgId) {
        int count = 0;
        switch (DataSource.getDatabaseType()) {
            case DatabaseType.PostgreSql:
            case DatabaseType.MySql:
            case DatabaseType.Oracle: {
                count = access.nativeQueryScalar(int.class, """
                        SELECT CASE WHEN EXISTS (
                            SELECT 1 FROM "CA_DomainMessageFilter" WHERE "Id" = '%s'
                        ) THEN 1 ELSE 0 END AS IdExists;
                        """.formatted(msgId), null);
                break;
            }
            case DatabaseType.SqlServer: {
                count = access.nativeQueryScalar(int.class, """
                        SELECT CASE WHEN EXISTS (
                            SELECT 1 FROM "CA_DomainMessageFilter" WHERE [Id] = '%s'
                        ) THEN 1 ELSE 0 END AS IdExists;
                        """.formatted(msgId), null);
                break;
            }
        }
        return count > 0;
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
                access.nativeExecute("""
                        INSERT INTO "CA_DomainMessageFilter"
                                  ("Id")
                            VALUES
                                  ('%s');
                        """.formatted(msgId));
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

                access.nativeExecute("""
                        INSERT INTO [dbo].[CA_DomainMessageFilter]
                                  ([Id])
                            VALUES
                                  ('%s');
                        """.formatted(msgId));

                break;
            }
        }
    }

    public static void deleteExpired() {
        // 删除文件后，删除数据库数据
        DataContext.newScope((conn) -> {
            deleteExpired(conn.access());
        });
    }

    private static void deleteExpired(DataAccess access) {
        switch (DataSource.getDatabaseType()) {
            case DatabaseType.PostgreSql: {
                access.nativeExecute("""
                        DELETE FROM "CA_DomainMessageFilter" WHERE "CreateTime" < NOW() - INTERVAL '10 days';
                        """);

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

                access.nativeExecute("DELETE FROM [dbo].[CA_DomainMessageFilter] WHERE CreateTime < DATEADD(DAY, -10, GETDATE());");
                break;
            }
        }
    }


    public static void inited() {
        start(); //开始监控过期
    }

    public static void dispose() {
        end();
    }

    private static final Timer _scheduler = new Timer(1, TimeUnit.DAYS);

    private static void start() {
        _scheduler.immediate(() -> {
            deleteExpired();
        });
    }

    private static void end() {
        _scheduler.stop();
    }

}
