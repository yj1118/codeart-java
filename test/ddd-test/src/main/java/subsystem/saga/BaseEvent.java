package subsystem.saga;

import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.ddd.saga.EventContext;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

import java.util.Arrays;

@SafeAccess
public abstract class BaseEvent extends DomainEvent {

    private boolean execBeforeThrowError(DTObject arg) {
        return arg.getBoolean("execBeforeThrowError", false);
    }

    protected void tryExecBeforeThrowError(DTObject arg) {
        if (execBeforeThrowError(arg)) throw new IllegalStateException("execBeforeThrowError");
    }

    private boolean execAfterThrowError(DTObject arg) {
        return arg.getBoolean("execAfterThrowError", false);
    }

    protected void tryExecAfterThrowError(DTObject arg) {
        if (execAfterThrowError(arg)) throw new IllegalStateException("execAfterThrowError");
    }

    public DTObject getResult(int value, DTObject arg) {
        var result = DTObject.value("value", value);
        var remoteNodes = arg.getList("nodes", false);
        if (remoteNodes != null && remoteNodes.size() > 0) {
            remoteNodes.remove(0);
            result.pushObjects("remoteNodes", "{eventName,status}", remoteNodes);
        }
        return result;
    }

}
