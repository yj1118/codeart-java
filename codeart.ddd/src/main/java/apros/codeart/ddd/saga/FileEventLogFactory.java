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
}
