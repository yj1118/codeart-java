package apros.codeart.gate.service;

public final class ServiceProxyFactory {

	private ServiceProxyFactory() {
	}

	private static IServiceProxy _proxy;

	public static IServiceProxy get() {
		return _proxy;
	}

	public static void register(IServiceProxy proxy) {
		_proxy = proxy;
	}

}
