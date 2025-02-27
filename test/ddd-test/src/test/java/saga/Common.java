package saga;

import apros.codeart.ddd.command.EventCallable;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccess;
import subsystem.saga.*;

import java.nio.file.Path;
import java.util.ArrayList;

public final class Common {

    private Common() {
    }

    public static void init() {
        var user = DTObject.editable();
        user.setLong("id", 1);
        BaseEvent.saveUser(user);
    }

    public static class Config {


        private final NodeStatus _localStatus;

        private final RemoteNode[] _remoteNodes;

        public RemoteNode[] remoteNodes() {
            return _remoteNodes;
        }

        public Config(NodeStatus localStatus, RemoteNode[] remoteNodes) {
            _localStatus = localStatus;
            _remoteNodes = remoteNodes;
        }

        public DTObject getArg() {
            var arg = DTObject.editable();
            arg.setByte("status", _localStatus.getValue());
            for (var node : _remoteNodes) {
                arg.push("remoteNodes", node.to());
            }

            return arg;
        }
    }

    public static DTObject exec(Config config) {
        var arg = config.getArg();
        var result = EventCallable.execute(RegisterUserEvent.Name, arg);
        return result.getObject("user", DTObject.empty());
    }


    private final static String[] eventNames = new String[]{
            RegisterUserEvent.Name, OpenAccountEvent.Name, OpenWalletEvent.Name, CheckEmailEvent.Name, CompletedEvent.Name
    };

    public static String getEventName(int index) {
        return eventNames[index];
    }

    public static DTObject exec(NodeStatus... nodeStatuses) {

        var localStatus = nodeStatuses[0];

        ArrayList<RemoteNode> temp = new ArrayList<>();
        for (var i = 1; i < nodeStatuses.length; i++) {
            var eventName = eventNames[i];
            var status = nodeStatuses[i];
            var node = new RemoteNode(eventName, status);
            temp.add(node);
        }

        var remoteNodes = ListUtil.asArray(temp, RemoteNode.class);

        var config = new Config(localStatus, remoteNodes);
        return exec(config);
    }


    public static boolean isRegistered(DTObject user) {
        if (!user.exist("Register")) return false;
        return user.getInt("Register") == 1;
    }

    public static boolean isOpenWallet(DTObject user) {
        if (!user.exist("Wallet")) return false;
        return user.getInt("Wallet") == 1;
    }

    public static boolean isOpenAccount(DTObject user) {
        if (!user.exist("Account")) return false;
        return user.getInt("Account") == 1;
    }

    public static boolean isCheckEmail(DTObject user) {
        if (!user.exist("CheckEmail")) return false;
        return user.getInt("CheckEmail") == 1;
    }

    public static boolean isCompleted(DTObject user) {
        if (!user.exist("Completed")) return false;
        return user.getInt("Completed") == 1;
    }

    public static boolean isRegistered() {
        return isRegistered(BaseEvent.loadUser());
    }

    public static boolean isOpenAccount() {
        return isOpenAccount(BaseEvent.loadUser());
    }

    public static boolean isOpenWallet() {
        return isOpenWallet(BaseEvent.loadUser());
    }

    public static boolean isCheckEmail() {
        return isCheckEmail(BaseEvent.loadUser());
    }

    public static boolean isCompleted() {
        return isCompleted(BaseEvent.loadUser());
    }

}
