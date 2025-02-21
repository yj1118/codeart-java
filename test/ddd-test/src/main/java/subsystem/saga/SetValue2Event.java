package subsystem.saga;

import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.ddd.saga.EventContext;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class SetValue2Event extends DomainEvent {

    @Override
    public String name() {
        return "SetValue2Event";
    }

    @Override
    public DTObject raise(DTObject arg, EventContext ctx) {

        ctx.submit((log) -> {
            log.setInt("value", Accumulator.Instance.value());
        });

        var value = arg.getInt("value");
        // 什么都不做，只是返出去而已
        Accumulator.Instance.setValue(value);

        return Accumulator.Instance.toDTO();
    }


    @Override
    public void reverse(DTObject log) {
        if (log.isEmpty()) return;

        var oldValue = log.getInt("value");
        Accumulator.Instance.setValue(oldValue);
    }
}
