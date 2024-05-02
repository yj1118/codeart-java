package apros.codeart.ddd.message;

import java.util.List;

import apros.codeart.ddd.message.internal.MessageEntry;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.DataAccess;
import apros.codeart.ddd.repository.access.DataSource;
import apros.codeart.ddd.repository.access.DatabaseType;
import apros.codeart.dto.DTObject;
import apros.codeart.io.IOUtil;
import apros.codeart.util.ListUtil;

class FileMessageLogger implements IMessageLog {

	private FileMessageLogger() {
	}

	public static final FileMessageLogger instance = new FileMessageLogger();

	@Override
	public void write(String id, String name, DTObject content) {

		// 注意，写入文件前，先要写入数据库数据
		DataContext.using((access) -> {
			// 跟主程序同一个事务，确保两者都被同时提交
			insert(access, id);
		});

		var fileName = IOUtil.combine(_rootFolder, id, name).toAbsolutePath().toString();
		IOUtil.atomicWrite(fileName, content.getCode());

	}

	@Override
	public void flush(String id) {
		// 删除即可
		var folder = IOUtil.combine(_rootFolder, id).toAbsolutePath().toString();
		IOUtil.delete(folder);

		// 删除文件后，删除数据库数据
		DataContext.newScope((access) -> {
			delete(access, id);
		});
	}

	@Override
	public List<String> findInterrupteds() {
		// 注意，要从数据库里读取，只有数据库里的是必须要发送的，而不是文件里存储的
		var ids = DataContext.using((access) -> {
			return findInterrupteds(access);
		});
		return ListUtil.asList(ids);
	}

	@Override
	public MessageEntry find(String id) {

		var namePath = IOUtil.firstFile(IOUtil.combine(_rootFolder, id));

		if (namePath == null)
			return null;

		var name = namePath.getFileName().toString();

		var contentCode = IOUtil.readString(IOUtil.combine(_rootFolder, id, name));
		var content = DTObject.readonly(contentCode);
		return new MessageEntry(name, id, content);
	}

	@Override
	public void cleanup() {
		// 注意，仅清理文件，数据库里的数据由恢复器恢复发送后删除
		// 这里只删除文件，因为发送后还由文件存留，那么这些文件就是不需要发送的消息文件，需要清理
		IOUtil.delete(_rootFolder);
	}

	private static final String _rootFolder;

	static {
		var folder = MessageConfig.section().getString("log.folder", null);

		_rootFolder = folder == null
				? IOUtil.combine(IOUtil.getCurrentDirectory(), "domain-message-log").toAbsolutePath().toString()
				: folder;
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
					""".formatted(msgId), false);

			break;
		}
		}
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

			access.execute(String.format("DELETE FROM [dbo].[CA_DomainMessage] WHERE id='%s';", msgId), false);

			break;
		}
		}
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
