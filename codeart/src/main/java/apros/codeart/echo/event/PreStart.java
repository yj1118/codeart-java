package apros.codeart.echo.event;

import apros.codeart.ActionPriority;
import apros.codeart.ModuleInstaller;
import apros.codeart.PreApplicationStart;
import apros.codeart.echo.EchoConfig;
import apros.codeart.rabbitmq.event.RabbitMQEventProvider;

@PreApplicationStart(ActionPriority.Low)
public class PreStart {
	public static void initialize() {
		var providerName = EchoConfig.eventSection().getString("provider", null);
		// 安装事件节点下的提供者，该提供者一定安装的是订阅和分发事件的模块
		ModuleInstaller.setup(providerName, RabbitMQEventProvider.class);
	}
}
