package apros.codeart.ddd.saga.internal;

import apros.codeart.dto.DTObject;

public final class RaisedQueue {

	private String _id;

	public String id() {
		return _id;
	}

	private DTObject _identity;

	public DTObject identity() {
		return _identity;
	}

	public RaisedEntry next() {

	}

}
