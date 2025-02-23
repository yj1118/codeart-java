package subsystem.saga;

import apros.codeart.ddd.launcher.DomainContainer;
import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.ddd.saga.EventContext;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class AddUpEvent extends BaseEvent {

    @Override
    public String name() {
        return "AddUpEvent";
    }

    @Override
    public DTObject raise(DTObject arg, EventContext ctx) {

        var value = arg.getInt("value");

        this.tryExecBeforeThrowError(arg);
        value++;
        this.tryExecAfterThrowError(arg);

        DomainContainer.println("AddUpEvent_raise");

        return DTObject.value("value", value);
    }

    @Override
    public void reverse(DTObject log) {
        DomainContainer.println("AddUpEvent_reverse");
    }
}
