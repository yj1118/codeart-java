package apros.codeart.ddd.saga.internal;

import apros.codeart.IModuleProvider;
import apros.codeart.ddd.saga.EventLogFactory;
import apros.codeart.util.SafeAccess;

@SafeAccess
public final class FileEventLogProvider implements IModuleProvider {

	@Override
	public String name() {
		return "file-event.log";
	}

	@Override
	public void setup() {
		EventLogFactory.register(FileEventLogFactory.Instance);
	}
}
