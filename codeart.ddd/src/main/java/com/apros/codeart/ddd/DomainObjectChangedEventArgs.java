package com.apros.codeart.ddd;

public class DomainObjectChangedEventArgs {

	private DomainObject _source;

	public DomainObject source() {
		return _source;
	}

	public DomainObjectChangedEventArgs(DomainObject source) {
		_source = source;
	}
}
