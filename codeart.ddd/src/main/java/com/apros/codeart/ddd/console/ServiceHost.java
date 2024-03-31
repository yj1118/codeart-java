package com.apros.codeart.ddd.console;

public class ServiceHost {
	internal static bool IsEnabled
	{
	    get;
	    set;
	}


	public static void Start(Action initialize = null)
	{
	    IsEnabled = false;

//	    RPCEvents.ServerOpened += OnServerOpened;
//	    RPCEvents.ServerError += OnServerError;
//	    RPCEvents.ServerClosed += OnServerClosed;

	    AppInitializer.Initialize();

	    if (initialize != null)
	        initialize();

	    AppInitializer.Initialized();

	    Console.WriteLine(MQ.Strings.CloseServiceHost);

	    IsEnabled = true;
	    Console.ReadLine();


	    AppInitializer.Cleanup();

	    IsEnabled = false;
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
