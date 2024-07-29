package apros.codeart.ddd.service;

import static apros.codeart.i18n.Language.strings;

import java.util.TreeMap;
import java.util.Map;

import apros.codeart.runtime.Activator;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccessImpl;
import apros.codeart.util.StringUtil;

public final class ServiceProviderFactory {
    private ServiceProviderFactory() {
    }

    private static final Map<String, ServiceEntry> _map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static ServiceEntry get(String serviceName) {
        return _map.get(serviceName);
    }

    public static Iterable<ServiceEntry> getAll() {
        return _map.values();
//        return ListUtil.map(_map.entrySet(), (item) -> {
//            return new ServiceEntry(item.getKey(), item.getValue());
//        });
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
                throw new IllegalStateException(strings("apros.codeart.ddd", "TypeNotImple", serviceType.getSimpleName(),
                        IServiceProvider.class.getSimpleName()));
            }

            var service = (IServiceProvider) SafeAccessImpl.createSingleton(serviceType);

            var ann = serviceType.getAnnotation(Service.class);

            if (ann != null) {
                for (var serviceName : ann.value()) {
                    if (!StringUtil.isNullOrEmpty(serviceName)) {
                        _map.put(serviceName,
                                new ServiceEntry(serviceName, service));
                    }
                }

                var serviceName = serviceType.getSimpleName();
                _map.put(serviceName,
                        new ServiceEntry(serviceName, service));

            }
        }
    }

    static {
        collectServices();
    }

}
