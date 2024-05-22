package apros.codeart.ddd.service;

import static apros.codeart.i18n.Language.strings;

import java.util.HashMap;
import java.util.Map;

import apros.codeart.runtime.Activator;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccessImpl;

public final class ServiceProviderFactory {
	private ServiceProviderFactory() {
	}

	private static Map<String, IServiceProvider> _map = new HashMap<String, IServiceProvider>();

	public static IServiceProvider get(String serviceName) {
		return _map.get(serviceName);
	}

	public static Iterable<ServiceEntry> getAll() {
		return ListUtil.map(_map.entrySet(), (item) -> {
			return new ServiceEntry(item.getKey(), item.getValue());
		});
	}

	public static record ServiceEntry(String name, IServiceProvider provider) {
	}

	/**
	 * 收集对外提供了哪些服务
	 */
	private static void collectServices() {
		var servicTypes = Activator.getAnnotatedTypesOf(Service.class, "service");
		for (var serviceType : servicTypes) {

			if (!IServiceProvider.class.isAssignableFrom(serviceType)) {
				throw new IllegalStateException(strings("codeart.ddd", "TypeNotImple", serviceType.getSimpleName(),
						IServiceProvider.class.getSimpleName()));
			}

			var service = (IServiceProvider) SafeAccessImpl.createSingleton(serviceType);

			var anns = serviceType.getAnnotationsByType(Service.class);
			for (var ann : anns) {
				var serviceName = ann.value();
				_map.put(serviceName, service);
			}
		}
	}

	static {
		collectServices();
	}

}
