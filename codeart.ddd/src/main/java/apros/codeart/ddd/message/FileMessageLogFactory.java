package apros.codeart.ddd.message;

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
		// 不需要初始化
	}

}
