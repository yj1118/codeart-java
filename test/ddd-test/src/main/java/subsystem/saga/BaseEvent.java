package subsystem.saga;

import apros.codeart.ddd.launcher.DomainContainer;
import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.ddd.saga.EventContext;
import apros.codeart.dto.DTObject;
import apros.codeart.io.IOUtil;
import apros.codeart.log.Logger;
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

        var statusCount = loadStatusCount();

        copy(statusCount, ctx);

        statusCount++;

        var status = getMarkStatusName();

        DomainContainer.println(status + ":before");
        tryExecBeforeThrowError(arg);
        tryExecBeforeTimeout(arg);

        saveStatusCount(statusCount);

        DomainContainer.println(status + ":after");
        tryExecAfterThrowError(arg);
        tryExecAfterTimeout(arg);

        return getResult(arg);
    }

    @Override
    public void reverse(DTObject log) {
        DomainContainer.println(this.name() + ":reverseBefore");
        restore(log);
        DomainContainer.println(this.name() + ":reverseAfter");
    }

    private boolean isNodeStatus(DTObject arg, NodeStatus status) {
        return arg.getByte("status", (byte) 0) == status.getValue();
    }

    // region 抛出异常


    protected void tryExecBeforeThrowError(DTObject arg) {
        if (isNodeStatus(arg, NodeStatus.ERROR_BEFORE))
            throw new IllegalStateException("execBeforeThrowError");
    }

    protected void tryExecAfterThrowError(DTObject arg) {
        if (isNodeStatus(arg, NodeStatus.ERROR_AFTER))
            throw new IllegalStateException("execAfterThrowError");
    }

    // endregion

    // region 超时

    protected void tryExecBeforeTimeout(DTObject arg) {
        if (isNodeStatus(arg, NodeStatus.TIMEOUT_BEFORE)) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void tryExecAfterTimeout(DTObject arg) {
        if (isNodeStatus(arg, NodeStatus.TIMEOUT_AFTER)) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // endregion

    public DTObject getResult(DTObject arg) {
        var result = DTObject.editable();

        result.setObject("user", loadUser());

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
        DomainContainer.println("userFileName:" + fileName);
        var dto = DTObject.load(fileName);
        return dto.isEmpty() ? DTObject.editable() : dto.asEditable();
    }

    public int loadStatusCount() {
        var user = loadUser();
        var status = getMarkStatusName();

        return user.getInt(status, 0);
    }

    public void saveStatusCount(int statusCount) {
        var user = loadUser();
        var status = getMarkStatusName();
        user.setInt(status, statusCount);
        saveUser(user);
    }

    public static void saveUser(DTObject user) {
        var fileName = IOUtil.createTempFile("saga_user", true);
        user.save(fileName);
        DomainContainer.println("userSavedCode:" + loadUser().getCode(false, false));
    }


    protected void copy(int statusCount, EventContext ctx) {
        ctx.submit((log) -> {
            log.setInt("statusCount", statusCount); // 只用记录下，其实用不到
        });
    }

    protected void restore(DTObject log) {
        if (log.isEmpty()) return;

        var user = loadUser();
        DomainContainer.println(this.name() + ":current - " + user.getCode(false, false));
        var statusCount = log.getInt("statusCount", 0);
        saveStatusCount(statusCount);

        DomainContainer.println(this.name() + ":reverseSuccess - " + user.getCode(false, false));
    }


    protected abstract String getMarkStatusName();

}
