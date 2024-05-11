package apros.codeart.ddd.service;

public final class ServicePublisherFactory {

	private ServicePublisherFactory() {
	}

	private static IServicePublisher _publisher;

	public static IServicePublisher get() {
		return _publisher;
	}

	public static void register(IServicePublisher publisher) {
		_publisher = publisher;
	}

}
