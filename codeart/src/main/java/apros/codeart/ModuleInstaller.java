package apros.codeart;

import apros.codeart.runtime.Activator;
import apros.codeart.util.SafeAccessImpl;
import apros.codeart.util.StringUtil;

/**
 * 模块安装器，通过找到一个提供者来安装模块，安装的是什么模块由提供者来定
 */
public final class ModuleInstaller {

	private ModuleInstaller() {
	}

	/**
	 * 
	 * 安装模块提供者
	 * 
	 * @param providerName        提供者名称
	 * @param defaultProviderType 当没有配置时，默认使用的提供者的类型
	 */
	public static void setup(String providerName, Class<? extends IModuleProvider> defaultProviderType) {
		if (StringUtil.isNullOrEmpty(providerName)) {
			var provider = SafeAccessImpl.createSingleton(defaultProviderType);
			provider.setup();
			return;
		}

		var providerTypes = Activator.getSubTypesOf(IModuleProvider.class);
		for (var type : providerTypes) {
			var provider = SafeAccessImpl.createSingleton(type);
			if (provider.name().equalsIgnoreCase(providerName)) {
				provider.setup();
				return;
			}
		}
	}

}
