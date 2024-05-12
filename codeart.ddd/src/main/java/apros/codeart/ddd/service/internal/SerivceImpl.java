package apros.codeart.ddd.service.internal;

import apros.codeart.App;
import apros.codeart.ddd.service.ServicePublisherFactory;

public final class SerivceImpl {
	private SerivceImpl() {
	}

	public static void initialize() {
		App.setup("service");
		ServicePublisherFactory.get().release();
	}
}
