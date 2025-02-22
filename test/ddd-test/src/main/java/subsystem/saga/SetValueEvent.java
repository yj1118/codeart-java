package subsystem.saga;

import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.ddd.saga.EventContext;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class SetValueEvent extends DomainEvent {

    @Override
    public String name() {
        return "SetValueEvent";
    }

    @Override
    public DTObject raise(DTObject arg, EventContext ctx) {

        var a = Accumulator.Instance;
        var oldValue = a.value();

        ctx.submit((log) -> {
            log.setInt("value", oldValue);
        });

        var value = arg.getInt("value");

        boolean before_error = arg.getBoolean("before_error", false);
        if (before_error) throw new IllegalStateException("before_error");

        a.setValue(value);

        boolean after_error = arg.getBoolean("after_error", false);
        if (after_error) throw new IllegalStateException("after_error");

        return DTObject.readonly("{value}", a);
    }


    @Override
    public void reverse(DTObject log) {
        if (log.isEmpty()) return;

        var oldValue = log.getInt("value");
        Accumulator.Instance.setValue(oldValue);
    }
}
