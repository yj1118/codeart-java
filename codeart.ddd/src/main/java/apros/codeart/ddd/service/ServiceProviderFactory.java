package apros.codeart.ddd.service;

import static apros.codeart.i18n.Language.strings;

import java.util.function.Function;

import apros.codeart.runtime.Activator;
import apros.codeart.util.LazyIndexer;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccessImpl;

public final class ServiceProviderFactory {
	private ServiceProviderFactory() {
	}

	private static final Function<String, IServiceProvider> _get = LazyIndexer.init((serviceName) -> {
		var type = ListUtil.find(Activator.getAnnotatedTypesOf(Service.class, "service"), (t) -> {
			var anns = t.getAnnotationsByType(Service.class);
			return ListUtil.contains(anns, (ann) -> {
				return ann.value().equalsIgnoreCase(serviceName);
			});
		});

		if (!type.isAssignableFrom(IServiceProvider.class)) {
			throw new IllegalStateException(strings("codeart.ddd", "TypeNotImple", type.getSimpleName(),
					IServiceProvider.class.getSimpleName()));
		}

		return (IServiceProvider) SafeAccessImpl.createSingleton(type);
	});

	public static IServiceProvider get(String serviceName) {
		return _get.apply(serviceName);
	}

}
