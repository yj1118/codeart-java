package apros.codeart.ddd.service.mq;

import apros.codeart.ddd.repository.DataContext;
import com.google.common.base.Strings;

import apros.codeart.App;
import apros.codeart.UIException;
import apros.codeart.ddd.service.ServiceProviderFactory;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.rpc.IRPCHandler;
import apros.codeart.i18n.Language;
import apros.codeart.log.Logger;
import apros.codeart.util.StringUtil;
import net.sf.jsqlparser.expression.ArrayExpression;

public class ServiceHandler implements IRPCHandler {

    @Override
    public DTObject process(String method, DTObject args) {

        if (!App.started())
            throw new UIException(Language.strings("apros.codeart.ddd", "StartingService"));

        var serviceName = method;
        var entry = ServiceProviderFactory.get(serviceName);
        var provider = entry.provider();
        
        return provider.invoke(args);
    }

    protected ServiceHandler() {
    }

    public static final ServiceHandler Instance = new ServiceHandler();

}
