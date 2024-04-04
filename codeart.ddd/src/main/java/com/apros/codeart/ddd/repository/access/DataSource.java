package com.apros.codeart.ddd.repository.access;

import static com.apros.codeart.runtime.Util.propagate;

import java.sql.Connection;
import java.sql.SQLException;

import com.apros.codeart.AppConfig;
import com.apros.codeart.ddd.DomainDrivenException;
import com.apros.codeart.i18n.Language;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

final class DataSource {
	private DataSource() {
	}

	private static class DataSourceHolder {
		private static final HikariDataSource INSTANCE;

		private static final DatabaseType TYPE;

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

		static {

			var db = AppConfig.section("db");
			if (db != null) {
				TYPE = getType(db.getString("type"));

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
}
