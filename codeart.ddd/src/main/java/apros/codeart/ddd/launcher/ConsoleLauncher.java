package apros.codeart.ddd.launcher;

import java.util.Scanner;

import apros.codeart.App;
import apros.codeart.IAppInstaller;
import apros.codeart.ddd.repository.DataContext;
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
public final class ConsoleLauncher {

    private ConsoleLauncher() {
    }

    public static void start() {
        start(new AppInstaller());
    }

    public static void start(IAppInstaller installer) {
        try {
            System.out.println(Language.strings("apros.codeart.ddd", "StartServiceHost"));

            RPCEvents.serverOpened.add(new ServerOpenedObserver());
            RPCEvents.serverError.add(new ServerErrorObserver());
            RPCEvents.serverClosed.add(new ServerClosedObserver());

            App.init(installer);

            // 对于inited事件，给予数据上下文环境，方便用户使用数据资源
            DataContext.using(App::inited);

            // 所有初始化工作完毕后，开通服务
            RPCServer.open();

            System.out.println(Language.strings("apros.codeart.ddd", "CloseServiceHost"));

            readLine();

            System.out.println(Language.strings("apros.codeart.ddd", "CloseingServiceHost"));

            App.dispose();

            App.disposed();

            System.out.println(Language.strings("apros.codeart.ddd", "ClosedServiceHost"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
            System.out.println(Language.strings("apros.codeart.ddd", "ServiceIsOpen", args.methodName()));
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
            System.out.println(Language.strings("apros.codeart.ddd", "ServiceIsClose", args.methodName()));
        }
    }

}
