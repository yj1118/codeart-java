package apros.codeart.ddd.service;

import java.util.Scanner;

import apros.codeart.App;
import apros.codeart.echo.rpc.RPCEvents;
import apros.codeart.echo.rpc.RPCEvents.ServerClosedArgs;
import apros.codeart.echo.rpc.RPCEvents.ServerErrorArgs;
import apros.codeart.echo.rpc.RPCEvents.ServerOpenedArgs;
import apros.codeart.echo.rpc.RPCServer;
import apros.codeart.i18n.Language;
import apros.codeart.util.IEventObserver;

/**
 * 基于命令行的启动器
 */
public final class ServiceHost {

	private ServiceHost() {
	}

	private static volatile boolean _isEnabled;

	public static boolean isEnabled() {
		return _isEnabled;
	}

	public static void start() {
		start(null);
	}

	public static void start(Runnable initialize) {
		_isEnabled = false;

		System.out.println(Language.strings("codeart.ddd", "StartServiceHost"));

		RPCEvents.serverOpened.add(new ServerOpenedObserver());
		RPCEvents.serverError.add(new ServerErrorObserver());
		RPCEvents.serverClosed.add(new ServerClosedObserver());

		// 要从框架/子系统/服务宿主 3大块里找定义
		App.initialize("codeart", "subsystem", "service");

		if (initialize != null)
			initialize.run();

		App.initialized();

		// 所有初始化工作完毕后，开通服务
		RPCServer.open();

		System.out.println(Language.strings("codeart.ddd", "CloseServiceHost"));

		_isEnabled = true;
		readLine();

		System.out.println(Language.strings("codeart.ddd", "CloseingServiceHost"));

		App.dispose();

		_isEnabled = false;

		App.disposed();

		System.out.println(Language.strings("codeart.ddd", "ClosedServiceHost"));
	}

	private static void readLine() {
		Scanner scanner = new Scanner(System.in);

		// 使用nextLine方法读取一行
		scanner.nextLine();

		// 关闭Scanner对象
		scanner.close();
	}

	private static class ServerOpenedObserver implements IEventObserver<ServerOpenedArgs> {

		@Override
		public void handle(Object sender, ServerOpenedArgs args) {
			System.out.println(Language.strings("codeart.ddd", "ServiceIsOpen", args.methodName()));
		}
	}

	private static class ServerErrorObserver implements IEventObserver<ServerErrorArgs> {

		@Override
		public void handle(Object sender, ServerErrorArgs args) {
			System.out.println(args.exception().getMessage());
		}
	}

	private static class ServerClosedObserver implements IEventObserver<ServerClosedArgs> {

		@Override
		public void handle(Object sender, ServerClosedArgs args) {
			System.out.println(Language.strings("codeart.ddd", "ServiceIsClose", args.methodName()));
		}
	}

}
