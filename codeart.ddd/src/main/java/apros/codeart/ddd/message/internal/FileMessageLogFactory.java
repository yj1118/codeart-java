package apros.codeart.ddd.message.internal;

import apros.codeart.ddd.message.IMessageLog;
import apros.codeart.ddd.message.IMessageLogFactory;
import apros.codeart.util.SafeAccess;

@SafeAccess
public final class FileMessageLogFactory implements IMessageLogFactory {
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
