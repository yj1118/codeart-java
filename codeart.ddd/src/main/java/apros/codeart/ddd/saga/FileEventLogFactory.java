package apros.codeart.ddd.saga;

import apros.codeart.util.SafeAccess;

@SafeAccess
final class FileEventLogFactory implements IEventLogFactory {
	private FileEventLogFactory() {
	}

	public static final IEventLogFactory instance = new FileEventLogFactory();

	@Override
	public IEventLog create() {
		return new FileEventLogger();
	}

	@Override
	public void init() {
		// 基于文件系统的日志不需要初始化
	}
}
