package subsystem.saga;

import apros.codeart.util.SafeAccess;

@SafeAccess
public class CompletedEvent extends BaseEvent {

    @Override
    public String name() {
        return CompletedEvent.Name;
    }

    @Override
    protected String getMarkStatusName() {
        return "Completed";
    }

    public static final String Name = "CompletedEvent";
}
