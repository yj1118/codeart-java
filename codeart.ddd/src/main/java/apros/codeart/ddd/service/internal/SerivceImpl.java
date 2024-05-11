package apros.codeart.ddd.service.internal;

import apros.codeart.AppConfig;
import apros.codeart.ModuleInstaller;
import apros.codeart.ddd.service.ServicePublisherFactory;
import apros.codeart.ddd.service.mq.ServicePublisherProvider;

public final class SerivceImpl {
	private SerivceImpl() {
	}

	public static void initialize() {
		setupPublisher();
		ServicePublisherFactory.get().release();
	}

	private static void setupPublisher() {
		var provider = AppConfig.section().getString("service.provider", null);
		ModuleInstaller.setup(provider, ServicePublisherProvider.class);
	}

}
