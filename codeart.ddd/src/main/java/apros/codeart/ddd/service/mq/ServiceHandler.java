package apros.codeart.ddd.service.mq;

import com.google.common.base.Strings;

import apros.codeart.UIException;
import apros.codeart.ddd.service.ServiceHost;
import apros.codeart.ddd.service.ServiceProviderFactory;
import apros.codeart.dto.DTObject;
import apros.codeart.echo.rpc.IRPCHandler;
import apros.codeart.i18n.Language;
import apros.codeart.log.Logger;
import apros.codeart.util.StringUtil;

public class ServiceHandler implements IRPCHandler {

	@Override
	public DTObject process(String method, DTObject args) {

		if (!ServiceHost.isEnabled())
			throw new UIException(Language.strings("codeart.ddd", "StartingService"));

		DTObject result = null;
		String error = null;

		try {
			var serviceName = args.getString("serviceName", StringUtil.empty());
			var data = args.getObject("data", DTObject.Empty);

			var provider = ServiceProviderFactory.get(serviceName);
			result = provider.invoke(data);

		} catch (Exception ex) {
			Logger.fatal(ex);
			error = ex.getMessage();
		}

		var reponse = DTObject.editable();
		if (!Strings.isNullOrEmpty(error)) {
			reponse.setString("error", error);
		}

		if (result != null)
			reponse.combineObject("data", result);

		return reponse;
	}

	protected ServiceHandler() {
	}

	public static final ServiceHandler Instance = new ServiceHandler();

}
