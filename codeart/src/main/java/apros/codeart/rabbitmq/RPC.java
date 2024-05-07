package apros.codeart.rabbitmq;

public final class RPC {

	private RPC() {
	}

	/**
	 * 事件采用的消息的策略
	 */
	public static final Policy Policy;

	static RPC() {
		Policy = getPolicy();
	}

	private static Policy GetPolicy() {
		var policy = RabbitMQConfiguration.Current.PolicyGroupConfig.GetPolicy("rpc");

		// 由于远程调用是一次性的,服务端也会应答消息，所以我们不需要发布者确认和持久化消息
		return new Policy(policy.Name, policy.Server, policy.User, policy.PrefetchCount, false, false);
	}

	public static string GetServerQueue(string method) {
		return _getServerQueue(method);
	}

	private static Func<string, string> _getServerQueue = LazyIndexer.Init<string, string>((method)=>
	{
		return string.Format("rpc-{0}", method);
	});
}
