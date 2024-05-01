package apros.codeart.ddd.message;

import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.DataSource;
import apros.codeart.ddd.repository.access.DatabaseType;
import apros.codeart.util.SafeAccess;

@SafeAccess
final class FileMessageLogFactory implements IMessageLogFactory {
	private FileMessageLogFactory() {
	}

	public static final IMessageLogFactory instance = new FileMessageLogFactory();

	@Override
	public IMessageLog create() {
		return FileMessageLogger.instance;
	}

	@Override
	public void init() {
		// 此处要初始化领域消息表
		DataContext.newScope((access) -> {
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

				access.execute(
						"""
						CREATE TABLE [dbo].[CA_DomainMessage](
							[id] [uniqueidentifier] NOT NULL,
						 CONSTRAINT [PK_CA_DomainMessage] PRIMARY KEY CLUSTERED
						(
							[id] ASC
						)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
						) ON [PRIMARY]
						""");

				break;
			}
			}
		});
	}
}
