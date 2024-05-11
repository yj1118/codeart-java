package apros.codeart.ddd.service.internal;

import apros.codeart.ddd.service.ServicePublisherFactory;

public final class SerivceImpl {
	private SerivceImpl() {
	}

	public static void initialize() {
		ServicePublisherFactory.get().release();
	}

}
