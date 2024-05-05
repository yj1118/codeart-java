package apros.codeart.ddd.message;

import apros.codeart.util.SafeAccess;

@SafeAccess
final class FileMessageLogFactory implements IMessageLogFactory {
	private FileMessageLogFactory() {
	}

	public static final IMessageLogFactory Instance = new FileMessageLogFactory();

	@Override
	public IMessageLog create() {
		return FileMessageLogger.Instance;
	}

	@Override
	public void init() {
		// 不需要初始化
	}

}
