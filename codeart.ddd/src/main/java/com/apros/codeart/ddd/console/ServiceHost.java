package com.apros.codeart.ddd.console;

import java.util.Scanner;

import com.apros.codeart.App;
import com.apros.codeart.i18n.Language;

public final class ServiceHost {

	private ServiceHost() {
	}

	private static boolean _isEnabled;

	public static boolean isEnabled() {
		return _isEnabled;
	}

	public static void start() {
		start(null);
	}

	public static void start(Runnable initialize) {
		_isEnabled = false;

		System.out.println(Language.strings("codeart.ddd", "StartServiceHost"));

//	    RPCEvents.ServerOpened += OnServerOpened;
//	    RPCEvents.ServerError += OnServerError;
//	    RPCEvents.ServerClosed += OnServerClosed;

		App.initialize("subsystem", "service");

		if (initialize != null)
			initialize.run();

		App.initialized();

		System.out.println(Language.strings("codeart.ddd", "CloseServiceHost"));

		_isEnabled = true;

		readLine();

		System.out.println(Language.strings("codeart.ddd", "CloseingServiceHost"));

		App.dispose();

		App.disposed();

		System.out.println(Language.strings("codeart.ddd", "ClosedServiceHost"));

		_isEnabled = false;
	}

	private static void readLine() {
		Scanner scanner = new Scanner(System.in);

		// 使用nextLine方法读取一行
		scanner.nextLine();

		// 关闭Scanner对象
		scanner.close();
	}

//	private static void OnServerError(object sender, RPCEvents.ServerErrorArgs arg)
//	{
//	    Console.WriteLine(arg.Exception.GetCompleteMessage());
//	}
//
//	private static void OnServerOpened(object sender, RPCEvents.ServerOpenedArgs arg)
//	{
//	    Console.WriteLine(string.Format(MQ.Strings.ServiceIsOpen, arg.MethodName));
//	}
//
//	private static void OnServerClosed(object sender, RPCEvents.ServerClosedArgs arg)
//	{
//	    Console.WriteLine(string.Format(MQ.Strings.ServiceIsClose, arg.MethodName));
//	}
}
