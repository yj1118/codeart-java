package apros.codeart.ddd.saga.internal;

import apros.codeart.ddd.saga.IEventLog;
import apros.codeart.ddd.saga.IEventLogFactory;
import apros.codeart.util.SafeAccess;

@SafeAccess
final class FileEventLogFactory implements IEventLogFactory {
	private FileEventLogFactory() {
	}

	public static final IEventLogFactory Instance = new FileEventLogFactory();

	@Override
	public IEventLog create() {
		return new FileEventLogger();
	}

	@Override
	public void init() {
		// 基于文件系统的日志不需要初始化
	}
}
