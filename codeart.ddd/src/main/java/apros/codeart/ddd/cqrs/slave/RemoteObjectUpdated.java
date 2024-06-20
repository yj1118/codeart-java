package apros.codeart.ddd.cqrs.slave;

import apros.codeart.ddd.AggregateRoot;
import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.cqrs.ActionName;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Repository;
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

            var repoitory = Repository.create(typeName);

            DataContext.using(() -> {

                var id = data.getValue("id");

                var obj = (AggregateRoot) repoitory.findRoot(id, QueryLevel.SINGLE);
                // 加载数据，并标记为已改变
                obj.load(data, true);

                repoitory.updateRoot(obj);
            });

        }
    }

    private static final RemoteObjectUpdatedHandler handler = new RemoteObjectUpdatedHandler();
}
