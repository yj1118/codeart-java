package apros.codeart;

import apros.codeart.runtime.Activator;

public abstract class AppInstallerBase implements IAppInstaller {

	protected boolean setupCustom(String instConfigPath, Object[] args) {
		// 如果配置了自定义了模块安装器
		var installer = findModuleInstaller(instConfigPath);
		if (installer != null) {
			installer.setup(args);
			return true;
		}
		return false;
	}

	private IModuleInstaller findModuleInstaller(String instConfigPath) {
		var installerClassName = AppConfig.section().getString(instConfigPath, null);
		if (installerClassName == null)
			return null;
		return Activator.createInstance(IModuleInstaller.class, installerClassName);
	}
}
