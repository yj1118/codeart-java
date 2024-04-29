package apros.codeart.ddd.saga.internal.protector;

import apros.codeart.dto.DTObject;

public final class EventReverseQueue {

	private String _id;

	public String id() {
		return _id;
	}

	private DTObject _identity;

	public DTObject identity() {
		return _identity;
	}

	public EventEntry next() {

	}

}
