package apros.codeart.gate.launcher;

import java.util.Scanner;

import apros.codeart.App;
import apros.codeart.IAppInstaller;
import apros.codeart.gate.FetchPortal;

/**
 * 基于命令行的启动器
 */
public final class ConsoleLauncher {

	private ConsoleLauncher() {
	}

	public static void start() {
		start(new AppInstaller());
	}

	public static void start(IAppInstaller installer) {

//		System.out.println(Language.strings("codeart.ddd", "StartServiceHost"));
//
//		RPCEvents.serverOpened.add(new ServerOpenedObserver());
//		RPCEvents.serverError.add(new ServerErrorObserver());
//		RPCEvents.serverClosed.add(new ServerClosedObserver());

		App.init(installer);

		App.initialized();

		// 所有初始化工作完毕后，开通服务
		FetchPortal.open();

//		System.out.println(Language.strings("codeart.ddd", "CloseServiceHost"));

		readLine();

//		System.out.println(Language.strings("codeart.ddd", "CloseingServiceHost"));

		App.dispose();

		App.disposed();

//		System.out.println(Language.strings("codeart.ddd", "ClosedServiceHost"));
	}

	private static void readLine() {
		Scanner scanner = new Scanner(System.in);

		// 使用nextLine方法读取一行
		scanner.nextLine();

		// 关闭Scanner对象
		scanner.close();
	}
//
//	private static class ServerOpenedObserver implements IEventObserver<ServerOpenedArgs> {
//
//		@Override
//		public void handle(Object sender, ServerOpenedArgs args) {
//			System.out.println(Language.strings("codeart.ddd", "ServiceIsOpen", args.methodName()));
//		}
//	}
//
//	private static class ServerErrorObserver implements IEventObserver<ServerErrorArgs> {
//
//		@Override
//		public void handle(Object sender, ServerErrorArgs args) {
//			System.out.println(args.exception().getMessage());
//		}
//	}
//
//	private static class ServerClosedObserver implements IEventObserver<ServerClosedArgs> {
//
//		@Override
//		public void handle(Object sender, ServerClosedArgs args) {
//			System.out.println(Language.strings("codeart.ddd", "ServiceIsClose", args.methodName()));
//		}
//	}

}
