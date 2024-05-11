package apros.codeart.ddd.launcher;

import apros.codeart.IAppInstaller;

public class AppInstaller implements IAppInstaller {

	@Override
	public String[] getArchives() {
		return new String[] { "codeart", "subsystem", "service" };
	}

}
