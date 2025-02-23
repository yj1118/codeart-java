package saga;

import apros.codeart.ddd.command.EventCallable;
import apros.codeart.dto.DTObject;
import subsystem.saga.Accumulator;
import subsystem.saga.RemoteNode;

public final class Common {

    private Common() {
    }

    public static void init() {
        Accumulator.Instance.setValue(0);
    }

    public static class Config {

        private final int _initialValue;

        public int initialValue() {
            return _initialValue;
        }

        private final boolean _execBeforeThrowError;

        public boolean execBeforeThrowError() {
            return _execBeforeThrowError;
        }

        private final boolean _execAfterThrowError;

        public boolean execAfterThrowError() {
            return _execAfterThrowError;
        }

        private final RemoteNode[] _remoteNodes;

        public RemoteNode[] remoteNodes() {
            return _remoteNodes;
        }

        public Config(int initialValue) {
            this(initialValue, false, false, RemoteNode.empty);
        }

        public Config(int initialValue, RemoteNode[] remoteNodes) {
            this(initialValue, false, false, remoteNodes);
        }

        public Config(int initialValue, boolean execBeforeThrowError, boolean execAfterThrowError) {
            this(initialValue, execBeforeThrowError, execAfterThrowError, RemoteNode.empty);
        }

        public Config(int initialValue, boolean execBeforeThrowError, boolean execAfterThrowError,
                      RemoteNode[] remoteNodes) {
            _initialValue = initialValue;
            _execBeforeThrowError = execBeforeThrowError;
            _execAfterThrowError = execAfterThrowError;
            _remoteNodes = remoteNodes;
        }

        public DTObject getArg() {
            var arg = DTObject.editable();
            arg.setInt("value", _initialValue);
            if (this.execBeforeThrowError()) arg.setBoolean("execBeforeThrowError", true);
            if (this.execAfterThrowError()) arg.setBoolean("execAfterThrowError", true);
            for (var node : _remoteNodes) {
                arg.push("remoteNodes", node.to());
            }

            return arg;
        }

    }

    public static int exec(Config config) {
        var arg = config.getArg();
        var result = EventCallable.execute("RegisterUserEvent", arg);
        return result.getInt("value");
    }

    public static int ERROR = 0;

    public static int SUCCESS = 1;

}
