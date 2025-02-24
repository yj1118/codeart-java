package subsystem.saga;


import apros.codeart.util.SafeAccess;

@SafeAccess
public class OpenWalletEvent extends BaseEvent {

    @Override
    public String name() {
        return OpenWalletEvent.Name;
    }

    @Override
    protected String getMarkStatusName() {
        return "Wallet";
    }

    public static final String Name = "OpenWalletEvent";

}
