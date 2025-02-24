package subsystem.saga;

import apros.codeart.ddd.launcher.DomainContainer;
import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.ddd.saga.EventContext;
import apros.codeart.dto.DTObject;
import apros.codeart.io.IOUtil;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccess;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

@SafeAccess
public abstract class BaseEvent extends DomainEvent {

    @Override
    public Iterable<String> getPostEvents(DTObject input) {
        // 注意，以下写法是1对多个后续，[b,c,d]
        // 那么就是  a-b -> a-c -> c-d
//        var nodes = RemoteNode.getNodes(input);
//        return ListUtil.map(nodes, RemoteNode::eventName);

        //目前是:a->b->c
        var nodes = RemoteNode.getNodes(input);
        if (nodes.length == 0) return ListUtil.empty();

        var list = new ArrayList<String>();
        list.add(nodes[0].eventName());
        return list;
    }

    @Override
    public DTObject raise(DTObject arg, EventContext ctx) {

        var user = loadUser();
        
        copy(user, ctx);

        var status = getMarkStatusName();

        var statusCount = user.getInt(status, 0);
        statusCount++;
        user.setInt(status, statusCount);

        DomainContainer.println(status + ":before");
        tryExecBeforeThrowError(arg);

        saveUser(user);

        DomainContainer.println(status + ":after");
        tryExecAfterThrowError(arg);

        return getResult(user, arg);
    }

    @Override
    public void reverse(DTObject log) {
        DomainContainer.println(this.name() + ":reverse");
        restore(log);
    }

    private boolean execBeforeThrowError(DTObject arg) {
        return arg.getByte("status", (byte) 0) == NodeStatus.ERROR_BEFORE.getValue();
    }

    protected void tryExecBeforeThrowError(DTObject arg) {
        if (execBeforeThrowError(arg)) throw new IllegalStateException("execBeforeThrowError");
    }

    private boolean execAfterThrowError(DTObject arg) {
        return arg.getByte("status", (byte) 0) == NodeStatus.ERROR_AFTER.getValue();
    }

    protected void tryExecAfterThrowError(DTObject arg) {
        if (execAfterThrowError(arg)) throw new IllegalStateException("execAfterThrowError");
    }

    public DTObject getResult(DTObject user, DTObject arg) {
        var result = DTObject.editable();

        result.setObject("user", user);

        var remoteNodes = arg.getList("remoteNodes", false);
        if (remoteNodes != null && remoteNodes.size() > 0) {

            //下个节点的状态
            result.setByte("status", remoteNodes.get(0).getByte("status"));

            remoteNodes.remove(0);
            result.pushObjects("remoteNodes", "{eventName,status}", remoteNodes);
        }
        return result;
    }

    public static DTObject loadUser() {
        var fileName = IOUtil.createTempFile("saga_user", true);
        var dto = DTObject.load(fileName);
        return dto.isEmpty() ? DTObject.editable() : dto.asEditable();
    }

    public static void saveUser(DTObject user) {
        var fileName = IOUtil.createTempFile("saga_user", true);
        user.save(fileName);
    }


    protected void copy(DTObject user, EventContext ctx) {
        ctx.submit((log) -> {
            log.setObject("user", user);
        });
    }

    protected void restore(DTObject log) {
        if (log.isEmpty()) return;

        var user = log.getObject("user");
        var status = getMarkStatusName();

        if (!user.exist(status)) {
            saveUser(user);
            return;
        }

        var statusCount = user.getInt(status);
        statusCount--;
        user.setInt(status, statusCount);

        saveUser(user);
    }


    protected abstract String getMarkStatusName();

}
