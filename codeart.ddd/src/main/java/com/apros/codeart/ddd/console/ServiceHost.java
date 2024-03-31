package com.apros.codeart.ddd.console;

import com.apros.codeart.App;

public class ServiceHost {

	private static boolean _isEnabled;

	public static boolean isEnabled() {
		return _isEnabled;
	}

	public static void Start() {
		_isEnabled = false;

//	    RPCEvents.ServerOpened += OnServerOpened;
//	    RPCEvents.ServerError += OnServerError;
//	    RPCEvents.ServerClosed += OnServerClosed;

		App.initialize();

		App.initialized();

		Console.WriteLine(MQ.Strings.CloseServiceHost);

		_isEnabled = true;
		Console.ReadLine();

		App.dispose();

		App.disposed();

		_isEnabled = false;
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
