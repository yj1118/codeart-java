package apros.codeart.ddd.message.internal;

import apros.codeart.IModuleProvider;
import apros.codeart.ddd.message.MessageLogFactory;
import apros.codeart.util.SafeAccess;

@SafeAccess
public final class FileMessageLogProvider implements IModuleProvider {

	@Override
	public String name() {
		return "file-message.log";
	}

	@Override
	public void setup() {
		MessageLogFactory.register(FileMessageLogFactory.Instance);
	}
}
