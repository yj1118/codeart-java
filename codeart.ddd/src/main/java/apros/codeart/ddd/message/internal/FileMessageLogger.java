package apros.codeart.ddd.message.internal;

import apros.codeart.ddd.message.IMessageLog;
import apros.codeart.ddd.message.MessageConfig;
import apros.codeart.dto.DTObject;
import apros.codeart.io.IOUtil;

class FileMessageLogger implements IMessageLog {

	private FileMessageLogger() {
	}

	public static final FileMessageLogger Instance = new FileMessageLogger();

	@Override
	public void write(String id, String name, DTObject content) {
		var fileName = IOUtil.combine(_rootFolder, id, name).toAbsolutePath().toString();
		IOUtil.atomicWrite(fileName, content.getCode());
	}

	@Override
	public void flush(String id) {
		// 删除即可
		var folder = IOUtil.combine(_rootFolder, id).toAbsolutePath().toString();
		IOUtil.delete(folder);
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
		var folder = MessageConfig.section().getString("@log.folder", null);

		_rootFolder = folder == null
				? IOUtil.combine(IOUtil.getCurrentDirectory(), "domain-message-log").toAbsolutePath().toString()
				: folder;
	}

}
