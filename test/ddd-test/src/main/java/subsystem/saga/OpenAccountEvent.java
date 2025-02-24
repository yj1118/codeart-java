package subsystem.saga;


import apros.codeart.util.SafeAccess;

@SafeAccess
public class OpenAccountEvent extends BaseEvent {

    @Override
    public String name() {
        return OpenAccountEvent.Name;
    }

    @Override
    protected String getMarkStatusName() {
        return "Account";
    }

    public static final String Name = "OpenAccountEvent";

}
