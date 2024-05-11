package apros.codeart.ddd.service.mq;

import apros.codeart.IModuleProvider;
import apros.codeart.ddd.service.ServicePublisherFactory;
import apros.codeart.util.SafeAccess;

@SafeAccess
public final class ServicePublisherProvider implements IModuleProvider {

	@Override
	public String name() {
		return "mq-service.publisher";
	}

	@Override
	public void setup() {
		ServicePublisherFactory.register(ServicePublisher.Instance);
	}
}
