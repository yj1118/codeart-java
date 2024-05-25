package apros.codeart.gate.launcher;

import apros.codeart.AppConfig;
import apros.codeart.AppInstallerBase;
import apros.codeart.gate.service.ServiceProxyFactory;
import apros.codeart.gate.service.mq.MQServiceProxy;

/**
 * 
 */
public class AppInstaller extends AppInstallerBase {

	public AppInstaller() {
	}

	@Override
	public String[] getArchives() {
		return AppConfig.mergeArchives("apros.codeart");
	}

	@Override
	public void setup(String moduleName, Object[] args) {

		switch (moduleName) {
		case "service":
			setupServiceModule(args);
			break;
		}

	}

	protected void setupServiceModule(Object[] args) {
		if (setupCustom("service.inst", args))
			return;
		ServiceProxyFactory.register(MQServiceProxy.Instance);
	}

	@Override
	public void init() {

	}

	@Override
	public void dispose() {

	}

}
