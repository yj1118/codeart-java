package subsystem.saga;

import apros.codeart.ddd.saga.EventContext;
import apros.codeart.dto.DTObject;
import apros.codeart.util.ListUtil;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class RegisterUserEvent extends BaseEvent {

    @Override
    public String name() {
        return RegisterUserEvent.Name;
    }

    @Override
    protected String getMarkStatusName() {
        return "Register";
    }

    public static final String Name = "RegisterUserEvent";

}
