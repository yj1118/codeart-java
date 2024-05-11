package apros.codeart.echo.rpc;

public final class RPCClientFactory {
	private RPCClientFactory() {
	}

	private static IClientFactory _factory;

	public static IClientFactory get() {
		return _factory;
	}

	/**
	 * 
	 * 注册服务工厂
	 * 
	 * @param factory
	 */
	public static void register(IClientFactory factory) {
		_factory = factory;
	}

}
