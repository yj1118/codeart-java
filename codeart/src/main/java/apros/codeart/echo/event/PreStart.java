package apros.codeart.echo.event;

import apros.codeart.ActionPriority;
import apros.codeart.PreApplicationStart;
import apros.codeart.echo.EchoConfig;
import apros.codeart.echo.IEchoProvider;
import apros.codeart.rabbitmq.event.RabbitMQEventProvider;
import apros.codeart.runtime.Activator;
import apros.codeart.util.SafeAccessImpl;
import apros.codeart.util.StringUtil;

@PreApplicationStart(ActionPriority.Low)
public class PreStart {
	public static void initialize() {
		setupProvier();
	}

	private static void setupProvier() {
		var providerName = EchoConfig.eventSection().getString("provider", "rabbitMQ-event");
		if (StringUtil.isNullOrEmpty(providerName)) {
			var provider = new RabbitMQEventProvider();
			provider.setup();
		}

		var providerTypes = Activator.getSubTypesOf(IEchoProvider.class);
		for (var type : providerTypes) {
			var provider = SafeAccessImpl.createSingleton(type);
			if (provider.name().equalsIgnoreCase(providerName)) {
				provider.setup();
				return;
			}
		}
	}

}
