package com.apros.codeart.util;

public class NotifyCollectionChangedEventArgs {

	private NotifyCollectionChangedAction _action;

	public NotifyCollectionChangedAction action() {
		return _action;
	}

	private Object _sender;

	public Object sender() {
		return _sender;
	}

	public NotifyCollectionChangedEventArgs(Object sender) {
		_sender = sender;
	}

}
