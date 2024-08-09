package apros.codeart.echo.rpc;

import apros.codeart.util.EventHandler;

public final class RPCEvents {
    private RPCEvents() {
    }

//	#region RPC服务器已打开的事件

    /// <summary>
    /// RPC服务器已打开的事件
    /// </summary>
    public static class ServerOpenedArgs {

        private String _methodName;

        public String methodName() {
            return _methodName;
        }

        public ServerOpenedArgs(String methodName) {
            _methodName = methodName;
        }
    }

    public static final EventHandler<ServerOpenedArgs> serverOpened = new EventHandler<ServerOpenedArgs>();

    static void raiseServerOpened(Object sender, ServerOpenedArgs arg) {
        serverOpened.raise(sender, () -> {
            return arg;
        });
    }

//	#endregion
//
//	#
//
//	region RPC服务器已关闭的事件

    /// <summary>
    /// RPC服务器已关闭的事件
    /// </summary>
    public static class ServerClosedArgs {
        private String _methodName;

        public String methodName() {
            return _methodName;
        }

        public ServerClosedArgs(String methodName) {
            _methodName = methodName;
        }
    }

    public static final EventHandler<ServerClosedArgs> serverClosed = new EventHandler<ServerClosedArgs>();

    public static void raiseServerClosed(Object sender, ServerClosedArgs arg) {
        serverClosed.raise(sender, () -> {
            return arg;
        });
    }

//	#endregion
//
//	#
//
//	region RPC服务器已关闭的事件

    /// <summary>
    /// RPC服务器已关闭的事件
    /// </summary>
    public static class ServerErrorArgs {

        private Throwable _exception;

        public Throwable exception() {
            return _exception;
        }

        public ServerErrorArgs(Throwable exception) {
            _exception = exception;
        }
    }

    public static final EventHandler<ServerErrorArgs> serverError = new EventHandler<ServerErrorArgs>();

    public static void raiseServerError(Object sender, ServerErrorArgs arg) {
        serverError.raise(sender, () -> {
            return arg;
        });
    }

//	#endregion

}
