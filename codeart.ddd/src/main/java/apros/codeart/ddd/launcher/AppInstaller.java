package apros.codeart.ddd.launcher;

import apros.codeart.AppConfig;
import apros.codeart.IAppInstaller;
import apros.codeart.IModuleInstaller;
import apros.codeart.ddd.message.MessageLogFactory;
import apros.codeart.ddd.message.internal.FileMessageLogFactory;
import apros.codeart.ddd.saga.EventLogFactory;
import apros.codeart.ddd.saga.internal.FileEventLogFactory;
import apros.codeart.echo.event.EventPortal;
import apros.codeart.echo.rpc.RPCClientFactory;
import apros.codeart.echo.rpc.RPCServerFactory;
import apros.codeart.rabbitmq.event.RabbitMQEventPublisherFactory;
import apros.codeart.rabbitmq.event.RabbitMQEventSubscriberFactory;
import apros.codeart.rabbitmq.rpc.RabbitMQRPCClientFactory;
import apros.codeart.rabbitmq.rpc.RabbitMQRPCServerFactory;
import apros.codeart.runtime.Activator;

/**
 * 
 */
public class AppInstaller implements IAppInstaller {

	public AppInstaller() {
	}

	@Override
	public String[] getArchives() {
		return AppConfig.mergeArchives("codeart", "subsystem", "service");
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
		}

	}

	protected void setupRPCModule(Object[] args) {
		if (setupByCustom("rpc.inst", args))
			return;
		RPCClientFactory.register(RabbitMQRPCClientFactory.Instance);
		RPCServerFactory.register(RabbitMQRPCServerFactory.Instance);
	}

	protected void setupEventModule(Object[] args) {
		if (setupByCustom("event.inst", args))
			return;
		EventPortal.register(RabbitMQEventPublisherFactory.Instance);
		EventPortal.register(RabbitMQEventSubscriberFactory.Instance);
	}

	/**
	 * 安装领域消息的模块配置
	 */
	protected void setupMessageModule(Object[] args) {
		if (setupByCustom("message.inst", args))
			return;
		// 注入消息日志工厂
		MessageLogFactory.register(FileMessageLogFactory.Instance);
	}

	protected void setupSAGAModule(Object[] args) {
		if (setupByCustom("saga.inst", args))
			return;
		// 安装saga模块
		// 注入领域事件的日志工厂
		EventLogFactory.register(FileEventLogFactory.Instance);
	}

	private boolean setupByCustom(String instConfigPath, Object[] args) {
		// 如果配置了自定义了模块安装器
		var installer = findModuleInstaller(instConfigPath);
		if (installer != null) {
			installer.setup(args);
			return true;
		}
		return false;
	}

	private IModuleInstaller findModuleInstaller(String instConfigPath) {
		var installerClassName = AppConfig.section().getString(instConfigPath, null);
		if (installerClassName == null)
			return null;
		return Activator.createInstance(IModuleInstaller.class, installerClassName);
	}

	@Override
	public void init() {

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
