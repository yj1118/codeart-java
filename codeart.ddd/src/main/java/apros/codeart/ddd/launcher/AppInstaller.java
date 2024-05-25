package apros.codeart.ddd.launcher;

import apros.codeart.AppConfig;
import apros.codeart.AppInstallerBase;
import apros.codeart.ddd.message.MessageLogFactory;
import apros.codeart.ddd.message.internal.FileMessageLogFactory;
import apros.codeart.ddd.saga.EventLogFactory;
import apros.codeart.ddd.saga.internal.FileEventLogFactory;
import apros.codeart.ddd.service.ServicePublisherFactory;
import apros.codeart.ddd.service.mq.ServicePublisher;
import apros.codeart.echo.event.EventPortal;
import apros.codeart.echo.rpc.RPCClientFactory;
import apros.codeart.echo.rpc.RPCServerFactory;
import apros.codeart.rabbitmq.event.RabbitMQEventPublisherFactory;
import apros.codeart.rabbitmq.event.RabbitMQEventSubscriberFactory;
import apros.codeart.rabbitmq.rpc.RabbitMQRPCClientFactory;
import apros.codeart.rabbitmq.rpc.RabbitMQRPCServerFactory;

/**
 * 
 */
public class AppInstaller extends AppInstallerBase {

	public AppInstaller() {
	}

	@Override
	public String[] getArchives() {
		return AppConfig.mergeArchives("apros.codeart", "subsystem", "service");
	}

	@Override
	public void setup(String moduleName, Object[] args) {

		switch (moduleName) {
		case "echo.rpc":
			setupRPCModule(args);
			break;
		case "echo.event":
			setupEventModule(args);
			break;
		case "message":
			setupMessageModule(args);
			break;
		case "sage":
			setupSAGAModule(args);
			break;
		case "service":
			setupServiceModule(args);
			break;
		}

	}

	protected void setupRPCModule(Object[] args) {
		if (setupCustom("rpc.inst", args))
			return;
		RPCClientFactory.register(RabbitMQRPCClientFactory.Instance);
		RPCServerFactory.register(RabbitMQRPCServerFactory.Instance);
	}

	protected void setupEventModule(Object[] args) {
		if (setupCustom("event.inst", args))
			return;
		EventPortal.register(RabbitMQEventPublisherFactory.Instance);
		EventPortal.register(RabbitMQEventSubscriberFactory.Instance);
	}

	/**
	 * 安装领域消息的模块配置
	 */
	protected void setupMessageModule(Object[] args) {
		if (setupCustom("message.inst", args))
			return;
		// 注入消息日志工厂
		MessageLogFactory.register(FileMessageLogFactory.Instance);
	}

	protected void setupSAGAModule(Object[] args) {
		if (setupCustom("saga.inst", args))
			return;
		// 安装saga模块
		// 注入领域事件的日志工厂
		EventLogFactory.register(FileEventLogFactory.Instance);
	}

	protected void setupServiceModule(Object[] args) {
		if (setupCustom("service.inst", args))
			return;
		ServicePublisherFactory.register(ServicePublisher.Instance);
	}

	@Override
	public void init() {

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
