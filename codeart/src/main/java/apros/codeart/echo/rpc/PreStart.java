package apros.codeart.echo.rpc;

import apros.codeart.ActionPriority;
import apros.codeart.ModuleInstaller;
import apros.codeart.PreApplicationStart;
import apros.codeart.echo.EchoConfig;
import apros.codeart.rabbitmq.rpc.RabbitMQRPCProvider;

@PreApplicationStart(ActionPriority.High)
public class PreStart {
	public static void initialize() {
		var providerName = EchoConfig.eventSection().getString("provider", null);
		// 安装rpc节点下的提供者，该提供者一定安装的是rpc服务端和客户端的实现
		ModuleInstaller.setup(providerName, RabbitMQRPCProvider.class);
	}
}
