package apros.codeart.ddd.cqrs.master;

import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.cqrs.ActionName;
import apros.codeart.ddd.internal.DTOMapper;
import apros.codeart.ddd.message.DomainMessage;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.rpc.IRPCHandler;

final class GetObject implements IRPCHandler {
    private GetObject() {
    }

    public DTObject process(String method, DTObject arg) {

        var name = arg.getString("name");
        var id = arg.getValue("id");

        var data = Forker.getObjectData(name, id);
		
        return data;
    }

    public static final GetObject Instance = new GetObject();
}