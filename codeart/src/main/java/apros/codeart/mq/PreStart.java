package apros.codeart.mq;

import apros.codeart.ActionPriority;
import apros.codeart.PreApplicationStart;
import apros.codeart.rabbitmq.RabbitMQProvider;
import apros.codeart.runtime.Activator;
import apros.codeart.util.SafeAccessImpl;
import apros.codeart.util.StringUtil;

@PreApplicationStart(ActionPriority.Low)
public class PreStart {
	public static void initialize() {
		setupProvier();
	}

	private static void setupProvier() {
		var providerName = MQConfig.provider();
		if (StringUtil.isNullOrEmpty(providerName) || providerName.equalsIgnoreCase("rabbitMQ")) {
			// 不用单例，可以回收，节约内存
			var provider = new RabbitMQProvider();
			provider.setup();
		}

		var providerTypes = Activator.getSubTypesOf(IMQProvider.class);
		for (var type : providerTypes) {
			var provider = SafeAccessImpl.createSingleton(type);
			if (provider.name().equalsIgnoreCase(providerName)) {
				provider.setup();
				return;
			}
		}
	}

}
