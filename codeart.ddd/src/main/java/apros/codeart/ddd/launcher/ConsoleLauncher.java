package apros.codeart.ddd.launcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

import apros.codeart.App;
import apros.codeart.IAppInstaller;
import apros.codeart.TestSupport;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.saga.SAGAEvents;
import apros.codeart.ddd.saga.SAGAEvents.RegisteredEventArgs;
import apros.codeart.echo.rpc.RPCEvents;
import apros.codeart.echo.rpc.RPCEvents.ServerClosedArgs;
import apros.codeart.echo.rpc.RPCEvents.ServerErrorArgs;
import apros.codeart.echo.rpc.RPCEvents.ServerOpenedArgs;
import apros.codeart.echo.rpc.RPCServer;
import apros.codeart.i18n.Language;
import apros.codeart.util.IEventObserver;

import static apros.codeart.runtime.Util.propagate;

/**
 * 基于命令行的启动器
 */
public final class ConsoleLauncher {

    private ConsoleLauncher() {
    }

    public static void start() {
        start(new AppInstaller(), null);
    }

    @TestSupport
    public static void start_container(String containerName) {
        start(new AppInstaller(), containerName);
    }

    public static void start(IAppInstaller installer, String containerName) {
        boolean isContainer = containerName != null;
        try {
            System.out.println(Language.strings("apros.codeart.ddd", "StartServiceHost"));

            RPCEvents.serverOpened.add(new ServerOpenedObserver());
            RPCEvents.serverError.add(new ServerErrorObserver());
            RPCEvents.serverClosed.add(new ServerClosedObserver());

            SAGAEvents.eventRegistered.add(new EventRegisteredObserver());

            App.init(installer);

            // 对于inited事件，给予数据上下文环境，方便用户使用数据资源
            DataContext.using(App::inited);

            // 所有初始化工作完毕后，开通服务
            RPCServer.open();

            System.out.println(Language.strings("apros.codeart.ddd", "CloseServiceHost"));

            if (isContainer)
                System.out.printf("[%s]started%n", containerName);  //通讯用

            readLine();

            System.out.println(Language.strings("apros.codeart.ddd", "CloseingServiceHost"));

            App.dispose();

            App.disposed();

            System.out.println(Language.strings("apros.codeart.ddd", "ClosedServiceHost"));

            if (isContainer)
                System.out.printf("[%s]stopped%n", containerName);  //通讯用   //通讯用

        } catch (Throwable e) {
            if (isContainer) {
                System.out.println(e.getMessage());  //通讯用   //通讯用
                return;
            }
            throw propagate(e);
        }
    }

    private static void readLine() {
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine(); // 这里会阻塞
        }
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

    private static class EventRegisteredObserver implements IEventObserver<RegisteredEventArgs> {

        @Override
        public void handle(Object sender, RegisteredEventArgs args) {
            System.out.println(Language.strings("apros.codeart.ddd", "RegisteredDomainEvent", args.eventName()));
        }
    }

}
