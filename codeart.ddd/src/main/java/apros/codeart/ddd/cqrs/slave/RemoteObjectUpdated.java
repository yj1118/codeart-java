package apros.codeart.ddd.cqrs.slave;

import apros.codeart.ddd.cqrs.ActionName;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.ddd.virtual.internal.VirtualRepository;
import apros.codeart.dto.DTObject;
import apros.codeart.util.SafeAccess;

class RemoteObjectUpdated {

    public static void subscribe(String typeName) {
        var messageName = ActionName.objectUpdated(typeName);
        DomainMessage.subscribe(messageName, handler);
    }

    public static void cancel(String typeName) {
        var messageName = ActionName.objectUpdated(typeName);
        DomainMessage.cancel(messageName);
    }

    @SafeAccess
    private static class RemoteObjectUpdatedHandler extends RemoteObjectHandler {

        @Override
        protected void handle(DTObject content) {

            var typeName = content.getString("typeName");
            var data = content.getObject("data");

            VirtualRepository.update(typeName, data);
        }
    }

    private static final RemoteObjectUpdatedHandler handler = new RemoteObjectUpdatedHandler();
}
