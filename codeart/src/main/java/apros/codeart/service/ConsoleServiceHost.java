package apros.codeart.service;

import java.io.Console;

import apros.codeart.App;
import apros.codeart.mq.rpc.server.RPCEvents;
import apros.codeart.mq.rpc.server.RPCEvents.ServerClosedArgs;
import apros.codeart.mq.rpc.server.RPCEvents.ServerErrorArgs;
import apros.codeart.mq.rpc.server.RPCEvents.ServerOpenedArgs;
import apros.codeart.util.IEventObserver;

public final class ConsoleServiceHost {

	private ConsoleServiceHost() {
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

		RPCEvents.serverOpened.add(new ServerOpenedObserver());
		RPCEvents.serverError.add(new ServerErrorObserver());
		RPCEvents.serverClosed.add(new ServerClosedObserver());

		App.initialize();

		if (initialize != null)
			initialize.run();

		App.initialized();

		Console.WriteLine(MQ.Strings.CloseServiceHost);

		_isEnabled = true;
		Console.ReadLine();

		App.dispose();

		_isEnabled = false;
	}

	private static class ServerOpenedObserver implements IEventObserver<ServerOpenedArgs> {

		@Override
		public void handle(Object sender, ServerOpenedArgs args) {
			Console.WriteLine(string.Format(MQ.Strings.ServiceIsOpen, arg.MethodName));

		}
	}

	private static class ServerErrorObserver implements IEventObserver<ServerErrorArgs> {

		@Override
		public void handle(Object sender, ServerErrorArgs args) {
			Console.WriteLine(arg.Exception.GetCompleteMessage());
		}
	}

	private static class ServerClosedObserver implements IEventObserver<ServerClosedArgs> {

		@Override
		public void handle(Object sender, ServerClosedArgs args) {
			Console.WriteLine(string.Format(MQ.Strings.ServiceIsClose, arg.MethodName));
		}
	}

}
