package apros.codeart.ddd.saga;


import apros.codeart.util.EventHandler;

public final class SAGAEvents {

    private SAGAEvents() {
    }

    //region 登记领域事件的事件


    public static class RegisteredEventArgs {

        private final String _eventName;

        public String eventName() {
            return _eventName;
        }

        public RegisteredEventArgs(String eventName) {
            _eventName = eventName;
        }
    }

    public static final EventHandler<SAGAEvents.RegisteredEventArgs> eventRegistered = new EventHandler<SAGAEvents.RegisteredEventArgs>();

    public static void raiseEventRegistered(Object sender, SAGAEvents.RegisteredEventArgs arg) {
        eventRegistered.raise(sender, () -> {
            return arg;
        });
    }

    //endregion
}
