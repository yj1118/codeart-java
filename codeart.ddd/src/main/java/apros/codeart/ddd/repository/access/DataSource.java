package apros.codeart.ddd.repository.access;

import static apros.codeart.runtime.Util.propagate;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import apros.codeart.AppConfig;
import apros.codeart.ddd.DomainDrivenException;
import apros.codeart.ddd.repository.sqlserver.SQLServerAgent;
import apros.codeart.i18n.Language;

public final class DataSource {
	private DataSource() {
	}

	private static class DataSourceHolder {
		private static final HikariDataSource INSTANCE;

		private static final DatabaseType TYPE;

		private static final IDatabaseAgent AGENT;

		private static DatabaseType getType(String dbType) {
			switch (dbType.toLowerCase()) {
			case "mysql":
				return DatabaseType.MySql;
			case "postgresql":
				return DatabaseType.PostgreSql;
			case "oracle":
				return DatabaseType.Oracle;
			case "sqlserver":
				return DatabaseType.SqlServer;
			}
			throw new DomainDrivenException(Language.strings("codeart.ddd", "UnsupportedDatabase"));
		}

		private static IDatabaseAgent getAgent(DatabaseType dbType) {
			switch (dbType) {
//			case DatabaseType.MySql:
//				return null;
//			case DatabaseType.PostgreSql:
//				return DatabaseType.PostgreSql;
//			case DatabaseType.Oracle:
//				return DatabaseType.Oracle;
			case DatabaseType.SqlServer:
				return SQLServerAgent.Instance;
			default:
				break;
			}
			throw new DomainDrivenException(Language.strings("codeart.ddd", "UnsupportedDatabase"));
		}

		static {

			var db = AppConfig.section("repository.db");
			if (db != null) {
				TYPE = getType(db.getString("type"));

				AGENT = getAgent(TYPE);

				// 创建HikariConfig配置对象
				HikariConfig config = new HikariConfig();
				config.setJdbcUrl(db.getString("url"));
				config.setUsername(db.getString("username"));
				config.setPassword(db.getString("password"));
				// 可以添加更多配置

				// 使用配置创建HikariDataSource实例
				INSTANCE = new HikariDataSource(config);
			} else {
				INSTANCE = null;
				TYPE = null;
				AGENT = null;
			}
		}
	}

	public static Connection getConnection() {
		try {
			return DataSourceHolder.INSTANCE.getConnection();
		} catch (SQLException e) {
			throw propagate(e);
		}
	}

	public static DatabaseType getDatabaseType() {
		return DataSourceHolder.TYPE;
	}

	public static IDatabaseAgent getAgent() {
		return DataSourceHolder.AGENT;
	}

	public static IQueryBuilder getQueryBuilder(Class<? extends IQueryBuilder> qbType) {
		var agent = DataSource.getAgent();
		return agent.getQueryBuilder(qbType);
	}

}
