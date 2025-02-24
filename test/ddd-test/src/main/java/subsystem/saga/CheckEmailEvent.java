package subsystem.saga;


import apros.codeart.util.SafeAccess;

@SafeAccess
public class CheckEmailEvent extends BaseEvent {

    @Override
    public String name() {
        return CheckEmailEvent.Name;
    }

    @Override
    protected String getMarkStatusName() {
        return "CheckEmail";
    }

    public static final String Name = "CheckEmailEvent";

}
