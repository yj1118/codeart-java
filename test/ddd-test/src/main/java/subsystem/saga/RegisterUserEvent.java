package subsystem.saga;

import apros.codeart.ddd.saga.EventContext;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class RegisterUserEvent extends BaseEvent {

    @Override
    public String name() {
        return "RegisterUserEvent";
    }

    @Override
    public Iterable<String> getPostEvents(DTObject input) {
        var nodes = RemoteNode.getNodes(input);
        return ListUtil.map(nodes, RemoteNode::eventName);
    }

    protected void copyValue(EventContext ctx) {
        var a = Accumulator.Instance;
        var oldValue = a.value();

        ctx.submit((log) -> {
            log.setInt("value", oldValue);
        });
    }

    @Override
    public DTObject raise(DTObject arg, EventContext ctx) {

        copyValue(ctx);

        var value = arg.getInt("value");

        tryExecBeforeThrowError(arg);
        Accumulator.Instance.setValue(value);
        tryExecAfterThrowError(arg);

        return getResult(Accumulator.Instance.value(), arg);
    }


    @Override
    public void reverse(DTObject log) {
        if (log.isEmpty()) return;

        var oldValue = log.getInt("value");
        Accumulator.Instance.setValue(oldValue);
    }
}
