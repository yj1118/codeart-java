package apros.codeart.echo.rpc;

import apros.codeart.rabbitmq.Policy;

public interface IServerFactory {

    void register(String method, IRPCHandler handler, Policy policy);

    IServer get(String method);

    Iterable<IServer> getAll();
}
